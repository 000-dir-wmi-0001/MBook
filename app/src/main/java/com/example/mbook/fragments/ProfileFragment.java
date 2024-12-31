package com.example.mbook.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mbook.R;
import com.example.mbook.models.UserProfile;
import com.example.mbook.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {

    private FirebaseAuth auth;
    private DatabaseReference db;

    private TextView emailField;
    private TextView phoneField;
    private TextView roleField;
    private TextView profileNameField;
    private ImageView profileImageView;
    private Button updateButton;
    private Button deleteButton;
    private Button logoutButton;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri profileImageUri;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();

        emailField = view.findViewById(R.id.email_id);
        phoneField = view.findViewById(R.id.phone_number);
        roleField = view.findViewById(R.id.role);
        profileNameField = view.findViewById(R.id.profile_name);
        profileImageView = view.findViewById(R.id.profile_image);
        updateButton = view.findViewById(R.id.update_acc);
        deleteButton = view.findViewById(R.id.delete_acc);
        logoutButton = view.findViewById(R.id.logout_button);

        profileImageView.setOnClickListener(v -> openImageChooser());

        if (auth.getCurrentUser() == null) {
            Utils.goToLogin(getActivity());
            return view;
        }

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

                        if (userProfile.getProfileImage() != null) {
                            Picasso.get().load(userProfile.getProfileImage())
                                    .placeholder(R.drawable.default_profile_image)
                                    .into(profileImageView);
                        }
                    } else {
                        showToast("User profile data is missing");
                    }
                } else {
                    showToast("User profile not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast("Error fetching user data: " + databaseError.getMessage());
            }
        });

        updateButton.setOnClickListener(v -> showEditProfileDialog(userRef));
        deleteButton.setOnClickListener(v -> disableUserAccount());
        logoutButton.setOnClickListener(v -> logout());

        return view;
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            profileImageUri = data.getData();
            profileImageView.setImageURI(profileImageUri);
        }
    }

    private void uploadImageToFirebase(Uri uri, DatabaseReference userRef) {
        if (uri != null) {
            String userId = auth.getCurrentUser().getUid();
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("profile_images/" + userId + ".jpg");

            storageRef.putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> {
                        storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            userRef.child("profileImage").setValue(downloadUri.toString())
                                    .addOnSuccessListener(aVoid -> showToast("Profile image updated successfully"))
                                    .addOnFailureListener(e -> showToast("Failed to update profile image URL: " + e.getMessage()));
                        }).addOnFailureListener(e -> showToast("Failed to get download URL: " + e.getMessage()));
                    })
                    .addOnFailureListener(e -> showToast("Failed to upload image: " + e.getMessage()));
        } else {
            showToast("No image selected");
        }
    }

    private void updateUserProfile(String name, String email, String phone) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            DatabaseReference userRef = db.child("users").child(user.getUid());

            if (!name.isEmpty()) {
                userRef.child("firstName").setValue(name);
            }
            if (!email.isEmpty()) {
                userRef.child("email").setValue(email);
            }
            if (!phone.isEmpty()) {
                userRef.child("phone").setValue(phone);
            }

            if (profileImageUri != null) {
                uploadImageToFirebase(profileImageUri, userRef);
            }

            showToast("Profile updated successfully");
        }
    }

    private void showEditProfileDialog(DatabaseReference userRef) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        builder.setView(dialogView);

        EditText editFname = dialogView.findViewById(R.id.edit_fname);
        EditText editEmail = dialogView.findViewById(R.id.edit_email);
        EditText editPhone = dialogView.findViewById(R.id.edit_phone);
        ImageView editProfileImage = dialogView.findViewById(R.id.edit_profile_image);
        Button saveButton = dialogView.findViewById(R.id.save_button);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button); // Add cancel button

        // Load current user data
        editFname.setText(profileNameField.getText().toString());
        editEmail.setText(emailField.getText().toString());
        editPhone.setText(phoneField.getText().toString());

        // Image selection
        editProfileImage.setOnClickListener(v -> openImageChooser());

        AlertDialog dialog = builder.create();

        saveButton.setOnClickListener(v -> {
            String name = editFname.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();

            updateUserProfile(name, email, phone);
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss()); // Handle cancel button

        dialog.show();
    }

    private void disableUserAccount() {
        String userId = auth.getCurrentUser().getUid();
        db.child("users").child(userId).child("isDisabled").setValue(true)
                .addOnSuccessListener(aVoid -> {
                    showToast("Account disabled. You cannot log in again.");
                    Utils.goToLogin(getActivity());
                })
                .addOnFailureListener(e -> showToast("Failed to disable account: " + e.getMessage()));
    }

    private void logout() {
        auth.signOut();
        showToast("Logged out successfully");
        Utils.goToLogin(getActivity());
    }

    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}
