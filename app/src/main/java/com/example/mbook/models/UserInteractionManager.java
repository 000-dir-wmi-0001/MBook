package com.example.mbook.models;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserInteractionManager {

    private final DatabaseReference userInteractionsRef;

    public UserInteractionManager() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userInteractionsRef = FirebaseDatabase.getInstance().getReference("userInteractions").child(userId);
    }

    public void setLibraryInteraction(String libraryId, boolean liked, boolean bookmarked) {
        DatabaseReference libraryRef = userInteractionsRef.child("libraries").child(libraryId);
        libraryRef.child("liked").setValue(liked);
        libraryRef.child("bookmarked").setValue(bookmarked);
    }

    public void setEpisodeInteraction(String episodeId, boolean listened) {
        DatabaseReference episodeRef = userInteractionsRef.child("episodes").child(episodeId);
        episodeRef.child("listened").setValue(listened);
    }
}
