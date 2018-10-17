package com.binal.chatapp;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by binal on 2/8/16.
 */
public class FriendsListAdapter extends ArrayAdapter {
    private LayoutInflater inflater;
    private ArrayList<Contact> items;

    public FriendsListAdapter(Activity activity, ArrayList<Contact> items) {
        super(activity, android.R.layout.activity_list_item, items);
        inflater = activity.getWindow().getLayoutInflater();
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        if (items != null) {
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(items.get(position).getName());
            ((TextView) convertView.findViewById(android.R.id.text2)).setText(items.get(position).getId());
        }

        return convertView;
    }
}