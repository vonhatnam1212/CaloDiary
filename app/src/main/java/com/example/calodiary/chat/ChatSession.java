package com.example.calodiary.chat;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.List;

public class ChatSession {
    private String sessionId;
    private String title;
    private List<ChatMessage> messages;

    @ServerTimestamp
    private Date timestamp;

    public ChatSession() {}

    public ChatSession(String sessionId, String title, List<ChatMessage> messages) {
        this.sessionId = sessionId;
        this.title = title;
        this.messages = messages;
        this.timestamp = new Date();
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
