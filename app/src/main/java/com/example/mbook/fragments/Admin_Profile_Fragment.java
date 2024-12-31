package com.example.mbook.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mbook.R;
import com.example.mbook.models.UserProfile;
import com.example.mbook.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class Admin_Profile_Fragment extends Fragment {

    private FirebaseAuth auth;
    private DatabaseReference db;

    private EditText profileNameField;
    private EditText roleField;
    private EditText emailField;
    private EditText phoneField;
    private ImageButton logoutButton;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin__profile_, container, false);

        // Initialize Firebase Auth and Realtime Database
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();

        // Initialize UI components
        profileNameField = view.findViewById(R.id.profile_name);
        roleField = view.findViewById(R.id.role);
        emailField = view.findViewById(R.id.email_id);
        phoneField = view.findViewById(R.id.phone_number);
        logoutButton = view.findViewById(R.id.logout_button);

        // Check if the user is authenticated
        if (auth.getCurrentUser() == null) {
            Utils.goToLogin(getActivity());
            return view;
        }

        // Load user data from Realtime Database
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference userRef = db.child("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                    if (userProfile != null) {
                        profileNameField.setText(userProfile.getFirstName() + " " + userProfile.getLastName());
                        emailField.setText(userProfile.getEmail());
                        phoneField.setText(userProfile.getPhone());
                        roleField.setText(userProfile.getRole());
                    } else {
                        Toast.makeText(getActivity(), "User profile data is missing", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "User profile not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Error fetching user data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Set up listeners for buttons
        logoutButton.setOnClickListener(v -> logout());

        return view;
    }

    private void logout() {
        auth.signOut();
        Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();
        Utils.goToLogin(getActivity());
    }
}
