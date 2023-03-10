package com.example.qrapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    BottomNavigationView nav_bar;//nav bar object
    ImageButton SCAN;// scan button object
    ImageButton MYPROFILE;// get to myprofile page
    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set profile button
        MYPROFILE = findViewById(R.id.button_MYprofile);
        //set scan button
        SCAN = findViewById(R.id.button_scan);
        //set nav_bar
        nav_bar = findViewById(R.id.nav_barview);
        nav_bar.setOnItemSelectedListener(navbar_listener);


        // start at menu
        nav_bar.setSelectedItemId(R.id.main_tab);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            // User is not signed in, force them to sign up using Firebase
            System.out.println("User is not signed in");
            startActivity(new Intent(MainActivity.this, SignUpActivity.class));
        }
        else {
            System.out.println("User is signed in");
        }

        //SCAN BUTTON
        //This takes the player to the Scanning[and Picture] activity
        SCAN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //NEW SCANNING ACTIVITY, might be easier to use an activity for this one
                Toast.makeText(MainActivity.this, "scanow", Toast.LENGTH_SHORT).show();
                Intent ScanIntent = new Intent(MainActivity.this, ScanActivity.class);
                startActivity(ScanIntent);
            }
        });



        // start at menu tab when created
        nav_bar.setSelectedItemId(R.id.main_tab);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame, new MainFragment())
                .commit();

        MYPROFILE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame, new MyProfileFragment())
                        .commit();
            }
        });

        }






    //THE NAVIGATION BAR items
    NavigationBarView.OnItemSelectedListener navbar_listener = new NavigationBarView.OnItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//            return false;


            Fragment selected = new MainFragment();
            //Fragment selected = null;
            // not really necessary to initially set to anything but places emphasis that the app loads to main fragment


            /*Overview:
            Code works by creating a new, respective frament when each of the items are clicked
            The clicked fragment is commited to the "frame" of the Main Activity->see activity_main.xml for frame
            There is always at least and only 1 fragment chosen at any given time.
            This is the fragment that is commited
             */


            switch (item.getItemId()){
                case R.id.leaderboard_tab:
                    selected = new RankFragment();
                    break;
                case R.id.main_tab:
                    selected = new MainFragment();
                    break;
                case R.id.map_tab:
                    selected = new MapFragment();
                    break;
                case R.id.search_tab:
                    selected = new SearchFragment();
                    break;
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame, selected).commit();//SHOW FRAGMENT

            return true;
        }

    };


}