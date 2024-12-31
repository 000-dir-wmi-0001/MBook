package com.example.mbook.utils;

import android.util.Log;

import com.example.mbook.models.UserProfile;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class UserProfileUtils {

    private static DatabaseReference db = FirebaseDatabase.getInstance().getReference();

    public static void saveUserProfile(String userId, UserProfile userProfile) {
        db.child("users").child(userId)
                .setValue(userProfile)
                .addOnSuccessListener(aVoid -> System.out.println("User profile saved successfully!"))
                .addOnFailureListener(e -> System.err.println("Error saving user profile: " + e.getMessage()));
    }

    public static void getUserProfile(String userId, OnSuccessListener<UserProfile> onSuccessListener, OnFailureListener onFailureListener) {
        db.child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                    onSuccessListener.onSuccess(userProfile);
                } else {
                    onSuccessListener.onSuccess(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("UserProfileUtils", "Error fetching user profile: " + databaseError.getMessage());
                onFailureListener.onFailure(databaseError.toException());
            }
        });
    }

    public interface OnSuccessListener<T> {
        void onSuccess(T result);
    }

    public interface OnFailureListener {
        void onFailure(Exception e);
    }
}


//package com.example.mbook.utils;
//
////public class UserProfileUtils {
////}
//import com.example.mbook.models.UserProfile;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//public class UserProfileUtils {
//
//    public static void saveUserProfile(String userId, UserProfile userProfile) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.collection("users").document(userId)
//                .set(userProfile)
//                .addOnSuccessListener(aVoid -> System.out.println("User profile saved successfully!"))
//                .addOnFailureListener(e -> System.err.println("Error saving user profile: " + e.getMessage()));
//    }
//    public static void getUserProfile(String userId, OnSuccessListener<UserProfile> onSuccessListener, OnFailureListener onFailureListener) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.collection("users").document(userId).get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    if (documentSnapshot.exists()) {
//                        UserProfile userProfile = documentSnapshot.toObject(UserProfile.class);
//                        onSuccessListener.onSuccess(userProfile);
//                    } else {
//                        onSuccessListener.onSuccess(null);
//                    }
//                })
//                .addOnFailureListener((com.google.android.gms.tasks.OnFailureListener) onFailureListener);
//    }
//
//    public interface OnSuccessListener<T> {
//        void onSuccess(T result);
//    }
//
//    public interface OnFailureListener {
//        void onFailure(Exception e);
//    }
//}
