package com.binal.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/*
*  This Activity lists out the friends the user has
*  depending on the mode (which is determined by the value obtained through the intent that called
*  this activity in the first place) it performs an additional function
*  when chatNow is selected from the home activity it passes mode as true
*  and thus this acts as a selection list
 */

public class FriendsList extends AppCompatActivity {
    private static final String logH = "FriendsList";
    ArrayList<Contact> contacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(logH, "Activity Created");

        setContentView(R.layout.activity_friends_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // obtain the intent
        Intent intent = getIntent();
        boolean use_selection = intent.getBooleanExtra("mode",false);
        contacts = (ArrayList<Contact>) intent.getSerializableExtra("contacts");

        // initialize the list view
        ListView list = (ListView) findViewById(android.R.id.list);
        list.setAdapter(new FriendsListAdapter(this, contacts));

        if(use_selection){
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Toast.makeText(FriendsList.this,"clicked : " + contacts.get(position).getName(),Toast.LENGTH_SHORT).show();
                    Intent intent1 = new Intent(FriendsList.this,ChatNow.class);
                    Log.d(logH,"Chat Selected : " + contacts.get(position).getId() + " - " + contacts.get(position).getName());

                    intent1.putExtra("user_B",contacts.get(position));
                    startActivity(intent1);
                }
            });
        }
    }

}
