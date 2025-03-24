package com.example.calodiary.chat;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class ChatMessage {
    private String message;
    private boolean isUser; // true = user, false = AI
    @ServerTimestamp
    private Date timestamp;

    public ChatMessage() {

    }

    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
        this.timestamp = new Date();
    }

    public String getMessage() {
        return message;
    }

    public boolean isUser() {
        return isUser;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
