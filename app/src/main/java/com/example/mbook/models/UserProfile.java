package com.example.mbook.models;

public class UserProfile {
    private String id; // Unique identifier for the user
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String role; // Role of the user (e.g., "admin" or "user")
    private long lastActive; // Timestamp for last active
    private boolean isActive; // Indicates if the user is active
    private String profileImage; // URL for the user's profile image
    private String userProfile; // Additional attribute for user profile

    // Default constructor required for Firebase serialization
    public UserProfile() {
    }

    // Constructor with all attributes
    public UserProfile(String id, String firstName, String lastName, String phone, String email, String role, long lastActive, boolean isActive, String profileImage, String userProfile) {
        this.id = id; // Initialize unique identifier
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.role = role;
        this.lastActive = lastActive;
        this.isActive = isActive;
        this.profileImage = profileImage;
        this.userProfile = userProfile; // Initialize user profile
    }

    // Constructor without role and profile image
    public UserProfile(String id, String firstName, String lastName, String phone, String email, long lastActive, boolean isActive) {
        this(id, firstName, lastName, phone, email, "user", lastActive, isActive, "", ""); // Default values
    }

    // Getters and setters
    public String getId() {
        return id; // Getter for unique ID
    }

    public void setId(String id) {
        this.id = id; // Setter for unique ID
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public long getLastActive() {
        return lastActive;
    }

    public void setLastActive(long lastActive) {
        this.lastActive = lastActive;
    }

    public String getProfileImage() {
        return profileImage; // Getter for profile image
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage; // Setter for profile image
    }

    public String getUserProfile() {
        return userProfile; // Getter for user profile
    }

    public void setUserProfile(String userProfile) {
        this.userProfile = userProfile; // Setter for user profile
    }
}

