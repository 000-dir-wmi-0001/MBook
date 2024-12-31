
package com.example.mbook.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;

public class Library implements Parcelable {
    private String id;
    private String name;
    private String category;
    private int likes;
    private String thumbnail; // Field for thumbnail URL
    private Map<String, Episode> episodes;

    // Default constructor
    public Library() {
        episodes = new HashMap<>();
    }

    // Parameterized constructor
    public Library(String id, String name, String category, int likes, String thumbnail) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.likes = likes;
        this.thumbnail = thumbnail;
        this.episodes = new HashMap<>();
    }

    // Parcelable constructor
    protected Library(Parcel in) {
        id = in.readString();
        name = in.readString();
        category = in.readString();
        likes = in.readInt();
        thumbnail = in.readString();

        // Handle map deserialization manually
        episodes = new HashMap<>();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            String key = in.readString();
            Episode value = in.readParcelable(Episode.class.getClassLoader());
            episodes.put(key, value);
        }
    }

    public static final Creator<Library> CREATOR = new Creator<Library>() {
        @Override
        public Library createFromParcel(Parcel in) {
            return new Library(in);
        }

        @Override
        public Library[] newArray(int size) {
            return new Library[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(category);
        dest.writeInt(likes);
        dest.writeString(thumbnail);

        // Handle map serialization manually
        dest.writeInt(episodes.size());
        for (Map.Entry<String, Episode> entry : episodes.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeParcelable(entry.getValue(), flags);
        }
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Map<String, Episode> getEpisodes() {
        return episodes;
    }

    public void setEpisodes(Map<String, Episode> episodes) {
        this.episodes = episodes;
    }

}
