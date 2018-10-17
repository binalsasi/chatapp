package com.binal.chatapp;

/**
 * Created by binal on 31/7/16.
 */
public class ChatHistoryItem extends Message {
    private int unreads;

    public int getUnreads() {
        return unreads;
    }

    public void setUnreads(int unreads) {
        this.unreads = unreads;
    }
}
