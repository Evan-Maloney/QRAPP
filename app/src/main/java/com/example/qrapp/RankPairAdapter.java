package com.example.qrapp;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * The RankPairAdapter allows for a customized display of the data in a RankPair.
 */
public class RankPairAdapter extends BaseAdapter {

    ArrayList<RankPair> items;
    Context mycontext;

    /**
     * Constructor for RankPairAdapter
     * @param items list of RankPairs
     * @param context context
     */
    public RankPairAdapter(ArrayList<RankPair> items, Context context) {
        super();
        this.mycontext = context;
        this.items = items;
    }

    @Override
    /**
     * Get the count of items
     * @return count
     */
    public int getCount() {
//        return items.size();
        return items.size();
    }

    @Override
    /**
     * Get the item at position i
     * @param i
     * @return item
     */
    public RankPair getItem(int i) {
        return items.get(i);
    }

    @Override
    /**
     * Get the item id at position i
     * @param i
     * @return item id
     */
    public long getItemId(int i) {
        return items.get(i).hashCode();
    }

    @Override
    /**
     * Get the view at position i and display RankTriple
     * @param i
     * @param view
     * @param viewGroup
     * @return row
     */
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = LayoutInflater.from(mycontext);

        View row = inflater.inflate(R.layout.item_rankpair, viewGroup, false);

        RankPair row_data = items.get(i);

        TextView rank = row.findViewById(R.id.pair_ranknumber);
        TextView name = row.findViewById(R.id.pair_playername);
        TextView score = row.findViewById(R.id.pair_codepoints);

        rank.setText(String.valueOf(i+1));
        name.setText(row_data.PlayerName);
        score.setText(String.valueOf(row_data.Number));


        return row;
    }

}
