package com.calmpuchia.userapp.models;

public class NotificationModel {
    private String id;
    private String userId;
    private String title;
    private String message;
    private String type; // "general", "order", "promotion"
    private long timestamp;
    private boolean read;

    public NotificationModel() {
        // Required empty constructor for Firestore
    }

    public NotificationModel(String userId, String title, String message, String type) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
        this.read = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}