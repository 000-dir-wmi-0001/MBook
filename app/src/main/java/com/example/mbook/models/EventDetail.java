package com.example.mbook.models;

public class EventDetail {
    private String eventName;
    private String eventDetails;
    private String eventDate;

    public EventDetail(String eventName, String eventDetails, String eventDate) {
        this.eventName = eventName;
        this.eventDetails = eventDetails;
        this.eventDate = eventDate;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventDetails() {
        return eventDetails;
    }

    public String getEventDate() {
        return eventDate;
    }
}
