package com.example.mbook.utils;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AdminUtils {
    private static DatabaseReference db = FirebaseDatabase.getInstance().getReference();

    // Assign admin role to a user
    public static void assignAdminRole(String userId) {
        db.child("users").child(userId).child("role")
                .setValue("admin")
                .addOnSuccessListener(aVoid -> System.out.println("Admin role assigned successfully!"))
                .addOnFailureListener(e -> System.err.println("Error assigning admin role: " + e.getMessage()));
    }

    // Method to grant admin privileges
    public static void grantAdminPrivileges(Context context, String userId) {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(context, "Invalid user ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("role", "admin");

        db.child("users").child(userId)
                .updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Admin privileges granted.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error granting admin privileges: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

//package com.example.mbook.utils;
//
//import android.content.Context;
//import android.widget.Toast;
//
//import com.google.firebase.firestore.FirebaseFirestore;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class AdminUtils {
//    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//    // Assign admin role to a user
//    public static void assignAdminRole(String userId) {
//        db.collection("users").document(userId)
//                .update("role", "admin")
//                .addOnSuccessListener(aVoid -> System.out.println("Admin role assigned successfully!"))
//                .addOnFailureListener(e -> System.err.println("Error assigning admin role: " + e.getMessage()));
//    }
//
//    // Method to grant admin privileges
//    public static void grantAdminPrivileges(Context context, String userId) {
//        if (userId == null || userId.isEmpty()) {
//            Toast.makeText(context, "Invalid user ID.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        Map<String, Object> updates = new HashMap<>();
//        updates.put("role", "admin");
//
//        db.collection("users").document(userId)
//                .update(updates)
//                .addOnSuccessListener(aVoid -> {
//                    Toast.makeText(context, "Admin privileges granted.", Toast.LENGTH_SHORT).show();
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(context, "Error granting admin privileges: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//    }
//}


//package com.example.mbook.utils;
//
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.firestore.FirebaseFirestore;
//import android.content.Context;
//import android.widget.Toast;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.firebase.firestore.FirebaseFirestore;
//import java.util.HashMap;
//import java.util.Map;
//
//public class AdminUtils {
//    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//    // Assign admin role to a user
//    public static void assignAdminRole(String userId) {
////        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.collection("users").document(userId)
//                .update("role", "admin")
//                .addOnSuccessListener(aVoid -> System.out.println("Admin role assigned successfully!"))
//                .addOnFailureListener(e -> System.err.println("Error assigning admin role: " + e.getMessage()));
//    }
//
//
//
//    // Method to grant admin privileges
//    public static void grantAdminPrivileges(Context context, String userId) {
//        if (userId == null || userId.isEmpty()) {
//            Toast.makeText(context, "Invalid user ID.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        Map<String, Object> updates = new HashMap<>();
//        updates.put("role", "admin");
//
//        db.collection("users").document(userId)
//                .update(updates)
//                .addOnSuccessListener(aVoid -> {
//                    Toast.makeText(context, "Admin privileges granted.", Toast.LENGTH_SHORT).show();
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(context, "Error granting admin privileges: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//
//    }
//
//}
