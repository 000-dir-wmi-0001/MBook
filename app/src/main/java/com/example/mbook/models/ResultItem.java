package com.example.mbook.models;

public class ResultItem {
    private String name;
    // Add other fields if needed

    public ResultItem() {
        // Default constructor required for calls to DataSnapshot.getValue(ResultItem.class)
    }

    public ResultItem(String name) {
        this.name = name;
        // Initialize other fields if needed
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Add getters and setters for other fields
}
