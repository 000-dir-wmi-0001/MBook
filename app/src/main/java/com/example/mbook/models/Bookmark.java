package com.example.mbook.models;

public class Bookmark {
    private String userId;
    private String audiobookId;

    // Default constructor required for Firestore serialization
    public Bookmark() {
    }

    public Bookmark(String userId, String audiobookId) {
        this.userId = userId;
        this.audiobookId = audiobookId;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAudiobookId() {
        return audiobookId;
    }

    public void setAudiobookId(String audiobookId) {
        this.audiobookId = audiobookId;
    }
}
