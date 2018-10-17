package com.binal.chatapp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ChoiceFormat;
import java.util.ArrayList;

public class AddFriends extends AppCompatActivity {
    private static final String logH = "AddFriends";
    ArrayList<Contact> contacts;
    EditText user_id;
    EditText username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(logH, "Activity Created");

        setContentView(R.layout.activity_add_friends);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        user_id = (EditText) findViewById(R.id.user_id_B);
        username = (EditText) findViewById(R.id.username);

        contacts = (ArrayList<Contact>) getIntent().getSerializableExtra("contacts");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void add(View view){
        String friend_user = user_id.getText().toString();
        String name = username.getText().toString();
        Log.d(logH,"Add Button Clicked; Entered : " + friend_user + " - " + name);

        if(friend_user.equals("") || name.equals("")){
            Toast.makeText(this,"No Name or ID Entered",Toast.LENGTH_SHORT).show();
        }
        else {
            Contact contact = new Contact();
            contact.setId(friend_user);
            contact.setName(name);
            addFriend(contact);
            user_id.setText("");
            username.setText("");
        }
    }

    public void addFriend(Contact contact) {
        boolean flag = false;
        if(contacts!=null) {
            for (Contact one : contacts) {
                if (one.getId().equals(contact.getId())) {
                    flag = true;
                    break;
                }
            }
        }
        if (flag) {
            Toast.makeText(this, "Already A Friend", Toast.LENGTH_SHORT).show();
        } else {

            HomeActivity.db.addContact(contact);
            Toast.makeText(this, "Friend Added with userID : " + contact.getId(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
