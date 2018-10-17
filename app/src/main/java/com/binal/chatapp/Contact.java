package com.binal.chatapp;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by binal on 19/7/16.
 */
public class Contact implements Serializable{
    private String name, id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
