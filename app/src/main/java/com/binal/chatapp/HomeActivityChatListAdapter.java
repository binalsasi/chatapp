package com.binal.chatapp;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by binal on 31/7/16.
 */
public class HomeActivityChatListAdapter extends ArrayAdapter{
    private LayoutInflater inflater;
    private ArrayList<ChatHistoryItem> items;

    public HomeActivityChatListAdapter(Activity activity, ArrayList<ChatHistoryItem> items){
        super(activity, android.R.layout.activity_list_item, items);
        inflater = activity.getWindow().getLayoutInflater();
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            convertView = inflater.inflate(R.layout.chat_history_listitem_layout, parent, false);
        }

        if(items != null){
            ((TextView) convertView.findViewById(R.id.name)).setText(items.get(position).getName());
            ((TextView) convertView.findViewById(R.id.last_msg)).setText(items.get(position).getMsg());
            if(items.get(position).getUnreads() == 0)
                ((TextView) convertView.findViewById(R.id.count)).setText("");
            else
                ((TextView) convertView.findViewById(R.id.count)).setText("" + items.get(position).getUnreads());

        }

        return convertView;
    }
}