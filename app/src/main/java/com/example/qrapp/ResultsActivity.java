package com.example.qrapp;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.Manifest;

public class ResultsActivity extends AppCompatActivity {
    String hashed;
    long score;
    String name;
    String visual;
    Boolean includeGeolocation = false; // init false
    String[] comments;
    String[] playersScanned;
    TextView textView;
    CheckBox checkBox;
    Button addPhoto; // TODO: addPhotoFragment -> CameraX integration
    Image image; //  init as null
    //GeoPoint geolocation = null;
    Double lat;
    Double lon;
    Button continueToPost;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            hashed = extras.getString("hashed");
            score = extras.getLong("score");
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Init db collectionsRefs
        db = FirebaseFirestore.getInstance();
        final CollectionReference collectionReferenceQR = db.collection("QRCodes");
        final CollectionReference collectionReferencePlayer = db.collection("Users");

        // TODO: Check hashed value here with DB query (Check if exists and if player has already scanned it) GOTO: QRProfile if exists and/or already scanned...
        DocumentReference QRCExists = db.collection("QRCodes").document(hashed);
        QRCExists.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) { // QRCode already exists...
                        Log.d("TAG", "DocumentSnapshot data: " + document.getData());

                        // TODO: Query collection QRCodes' Users or collection Users' QRCodes to see if player has already scanned...

                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });

//        db.collection("QRCodes")
//                .whereEqualTo("hashed",true)
//                .get()
//        if (QRCExists.get() != null) { // does this QRC already exist in the db
//            Query playerScannedQRC = collectionReferencePlayer.whereEqualTo("QRCodes", hashed);
//            if (playerScannedQRC.get() != null) { // has player scanned this QRC
//                finish(); // TODO: GOTO QRProfile instead of returning to MainFeed (later)
//                Log.d("TAG", "fuck up 1"+QRCExists.get());
//            }
//            else {
//                // add user to QRC's playersScanned, add QRC to user's QRCs..
//                Log.d("TAG", "fuck up 2");
//                finish();
//            }
//        }


        // TODO: Add photo (fml)
        addPhoto = (Button) findViewById(R.id.results_add_photo_btn);

        // Display score:
        setContentView(R.layout.activity_results);
        textView = (TextView) findViewById(R.id.results_points);
        textView.setText("Scanned code is worth:\n" + score + " points!");

        // Create name and visual icon for new QRCode
        name = createName(hashed);
        visual = createVisual(hashed);

        // TODO: FIGURE THE FUCK OUT HOW THIS WORKS IS IT THIS ONE OR THE OTHER ONE...
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            Boolean fineLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_FINE_LOCATION, false);
                            Boolean coarseLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_COARSE_LOCATION, false);
                            if (fineLocationGranted != null && fineLocationGranted) {
                                // Precise location access granted.
                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                // Only approximate location access granted.
                            } else {
                                // No location access granted.
                            }
                        }
                );

        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
        // Get geolocation...
        checkBox = (CheckBox) findViewById(R.id.results_checkbox);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (includeGeolocation == false) {
                    includeGeolocation = true;

                    // TODO: FIGURE OUT HOW THE FUCK THIS ONE WORKS TOO I DON'T KNOW WHICH ONE IS ACTUALLY GETTING THE PERMISSION...

                    if (ActivityCompat.checkSelfPermission(ResultsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ResultsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        locationPermissionRequest.launch(new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        });

                        return;
                    }
                    CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                            .addOnSuccessListener(ResultsActivity.this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    // Got last known location. In some rare situations this can be null.
                                    if (location != null) {
                                        lat = location.getLatitude();
                                        lon = location.getLongitude();
                                    }
                                    Log.w("TAG", "No current location could be found");
                                }
                            });
                }
                else { // reset
                    includeGeolocation =  false;
                    lat = null;
                    lon = null;
                    //geolocation = null;

                }
            }
        });

        // Update DB and return to MainFeed
        continueToPost = (Button) findViewById(R.id.results_continue_btn);
        continueToPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Send new QRCode to DB, update Player scanned QRCodes
                Map<String,Object> newQRC = new HashMap<>();

//                HashMap<String, String> nameDB = new HashMap<>();
                newQRC.put("Name", name);
//                HashMap<String, String> visualDB = new HashMap<>();
                newQRC.put("icon",visual);
//                HashMap<String, Number> scoreDB = new HashMap<>();
                newQRC.put("Points",score);
//                HashMap<String, String> hashedDB = new HashMap<>();
                newQRC.put("Hash", hashed);
//                HashMap<String, Location> locationDB = new HashMap<>();
                if (includeGeolocation && lat != null && lon != null) { // TODO: WHY THE FUCK IS IT NULL SOMETIMES??? MAYBE SLOW TO GET COORDS?? - CORDS ARE SET TO GOOGLE'S LOCATION FOR EMULATOR BTW.
                    GeoPoint geolocation = new GeoPoint(lat,lon);
                    Log.d("TAG", "GEOLOCATION "+geolocation);
                    newQRC.put("Geolocation", geolocation);
                }
                else {
                    newQRC.put("Geolocation", null);
                }

                // TODO: Image gets sent into its own collection to be implemented...
                // TODO: playersScanned array contains UserID...
                newQRC.put("Comments", comments);
                newQRC.put("playersScanned", playersScanned);

                // Write new QRC to DB
                // TODO: Update User's scannedQRCs' array...
                db.collection("QRCodes").document(hashed) // DocIDs will be set to hashed
                        .set(newQRC)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {

                                        Log.d("TAG", "DocumentSnapshot successfully written!");
                                    }
                                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("TAG", "Error writing document", e);
                            }
                        });

                finish();
            }
        });

    }

    private String createName(String hashed) {
        String hashedSubstring = hashed.substring(0,6);
        String QRName = "";

        // 16^6 = 16.8 million unique combos.
        HashMap<Character, String> hexMapName = new HashMap<Character, String>();
        hexMapName.put('0', "Listy");
        hexMapName.put('1', "City");
        hexMapName.put('2', "Alpha");
        hexMapName.put('3', "Bravo");
        hexMapName.put('4', "Charlie");
        hexMapName.put('5', "Delta");
        hexMapName.put('6', "Echo");
        hexMapName.put('7', "Foxtrot");
        hexMapName.put('8', "Golf");
        hexMapName.put('9', "Hotel");
        hexMapName.put('a', "India");
        hexMapName.put('b', "Juliet");
        hexMapName.put('c', "Kilo");
        hexMapName.put('d', "Lima");
        hexMapName.put('e', "Mike");
        hexMapName.put('f', "November");

        QRName = hexMapName.get(hashedSubstring.charAt(0))+" "+hexMapName.get(hashedSubstring.charAt(1))+hexMapName.get(hashedSubstring.charAt(2))+hexMapName.get(hashedSubstring.charAt(3))+hexMapName.get(hashedSubstring.charAt(4))+hexMapName.get(hashedSubstring.charAt(5));
        Log.d("QRName:", QRName);
        return QRName;
    }

    private String createVisual (String hashed){
        String hashedSubstring = hashed.substring(0,6);
        String QRVisual = "";

        // 16^4 = 65K combos (65K X 16.8 Million = 1.1*10^12 combos)
        HashMap<Character, String> hexMapHead = new HashMap<Character, String>();
        hexMapHead.put('0', "C|");
        hexMapHead.put('1', "[|");
        hexMapHead.put('2', "<|");
        hexMapHead.put('3', "E|");
        hexMapHead.put('4', "#|");
        hexMapHead.put('5', "(|");
        hexMapHead.put('6', "F|");
        hexMapHead.put('7', "{|");
        hexMapHead.put('8', "d");
        hexMapHead.put('9', "[I");
        hexMapHead.put('a', "<=|");
        hexMapHead.put('b', "+=|");
        hexMapHead.put('c', "*(|");
        hexMapHead.put('d', "<)");
        hexMapHead.put('e', "c|");
        hexMapHead.put('f', "*=|");

        HashMap<Character, String> hexMapEyes = new HashMap<Character, String>();
        hexMapEyes.put('0', ":");
        hexMapEyes.put('1', ";");
        hexMapEyes.put('2', "$");
        hexMapEyes.put('3', "B");
        hexMapEyes.put('4', "X");
        hexMapEyes.put('5', "K");
        hexMapEyes.put('6', ">:");
        hexMapEyes.put('7', ">;");
        hexMapEyes.put('8', ">B");
        hexMapEyes.put('9', ">X");
        hexMapEyes.put('a', "=");
        hexMapEyes.put('b', "%");
        hexMapEyes.put('c', ">%");
        hexMapEyes.put('d', ">=");
        hexMapEyes.put('e', "D");
        hexMapEyes.put('f', ">D");

        HashMap<Character, String> hexMapNose = new HashMap<Character, String>();
        hexMapNose.put('0', "c");
        hexMapNose.put('1', "<");
        hexMapNose.put('2', ">");
        hexMapNose.put('3', "v");
        hexMapNose.put('4', "O");
        hexMapNose.put('5', "o");
        hexMapNose.put('6', "*");
        hexMapNose.put('7', "-");
        hexMapNose.put('8', "u");
        hexMapNose.put('9', ")");
        hexMapNose.put('a', "(");
        hexMapNose.put('b', "7");
        hexMapNose.put('c', ".");
        hexMapNose.put('d', ",");
        hexMapNose.put('e', "^");
        hexMapNose.put('f', "'");

        HashMap<Character, String> hexMapMouth = new HashMap<Character, String>();
        hexMapMouth.put('0', "b");
        hexMapMouth.put('1', "B");
        hexMapMouth.put('2', "]");
        hexMapMouth.put('3', "[");
        hexMapMouth.put('4', ")");
        hexMapMouth.put('5', "(");
        hexMapMouth.put('6', "|");
        hexMapMouth.put('7', "L");
        hexMapMouth.put('8', "6");
        hexMapMouth.put('9', "{]"); // Mustaches
        hexMapMouth.put('a', "{[");
        hexMapMouth.put('b', "{)");
        hexMapMouth.put('c', "{(");
        hexMapMouth.put('d', "{|");
        hexMapMouth.put('e', "D");
        hexMapMouth.put('f', "{D");

        QRVisual = hexMapHead.get(hashedSubstring.charAt(0))+hexMapEyes.get(hashedSubstring.charAt(1))+hexMapNose.get(hashedSubstring.charAt(2))+hexMapMouth.get(hashedSubstring.charAt(3));
        Log.d("QRVisual:", QRVisual);
        return QRVisual;
    }

    // TODO: Functions
    private void getImage() {} // will return Image from CameraX fragment...


}
