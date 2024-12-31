package com.example.mbook.models;
import android.os.Parcel;
import android.os.Parcelable;

public class Episode implements Parcelable {
    private String title;
    private String audioUrl;
    private String textFile; // Updated field for text file URI or path
    private String thumbnailUrl; // New attribute for thumbnail URL

    // Default constructor required for calls to DataSnapshot.getValue(Episode.class)
    public Episode() {
        // Default constructor
    }

    // Constructor to initialize the fields
    public Episode(String title, String audioUrl, String textFile,String thumbnailUrl) {
        this.title = title;
        this.audioUrl = audioUrl;
        this.textFile = textFile;
        this.thumbnailUrl = thumbnailUrl; // Initialize thumbnail URL
    }

    // Constructor used for Parcelable
    protected Episode(Parcel in) {
        title = in.readString();
        audioUrl = in.readString();
        textFile = in.readString(); // Updated to read the textFile field
        thumbnailUrl = in.readString(); // Read thumbnail URL
    }

    public static final Creator<Episode> CREATOR = new Creator<Episode>() {
        @Override
        public Episode createFromParcel(Parcel in) {
            return new Episode(in);
        }

        @Override
        public Episode[] newArray(int size) {
            return new Episode[size];
        }
    };

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getTextFile() {
        return textFile; // Updated getter
    }

    public void setTextFile(String textFile) {
        this.textFile = textFile; // Updated setter
    }

    public String getThumbnailUrl() {
        return thumbnailUrl; // Getter for thumbnail URL
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl; // Setter for thumbnail URL
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(audioUrl);
        dest.writeString(textFile); // Updated to write the textFile field
        dest.writeString(thumbnailUrl); // Write thumbnail URL
    }
}
