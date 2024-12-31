package com.example.mbook.utils;

import com.example.mbook.models.Episode;
import com.example.mbook.models.Library;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseHelper {
    private static final DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    // Save a library
    public static void saveLibrary(Library library) {
        DatabaseReference libraryRef = database.child("libraries").child(library.getId());
        libraryRef.setValue(library)
                .addOnSuccessListener(aVoid -> {
                    // Handle success
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    // Save an episode
    public static void saveEpisode(String libraryId, String episodeId, Episode episode) {
        DatabaseReference episodeRef = database.child("libraries").child(libraryId).child("episodes").child(episodeId);
        episodeRef.setValue(episode)
                .addOnSuccessListener(aVoid -> {
                    // Handle success
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

}
