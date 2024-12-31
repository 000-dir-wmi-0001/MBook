package com.example.mbook.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mbook.DashboardActivity;
import com.example.mbook.R;
import com.example.mbook.models.Library;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Add_Contents_Fragment extends Fragment {

    private EditText libraryNameEditText, categoryTypeEditText;
    private ImageView libraryThumbnailImageView;
    private Button createLibraryButton, selectThumbnailButton, navigateToAddEpisodesButton;
    private Uri thumbnailUri;
    private CircularProgressIndicator progressIndicator; // Progress Indicator
    private View overlay; // Overlay view

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add__contents_, container, false);

        libraryNameEditText = view.findViewById(R.id.library_name);
        categoryTypeEditText = view.findViewById(R.id.category_type);
        createLibraryButton = view.findViewById(R.id.create_library_button);
        selectThumbnailButton = view.findViewById(R.id.select_thumbnail_button);
        navigateToAddEpisodesButton = view.findViewById(R.id.navigate_to_add_episodes_button);
        libraryThumbnailImageView = view.findViewById(R.id.library_thumbnail);

        // Initialize overlay and progress indicator
        overlay = view.findViewById(R.id.blur_overlay);
        progressIndicator = view.findViewById(R.id.progress_bar);

        selectThumbnailButton.setOnClickListener(v -> selectThumbnail());
        createLibraryButton.setOnClickListener(v -> createLibrary());
        navigateToAddEpisodesButton.setOnClickListener(v -> navigateToAddEpisodes());

        return view;
    }

    private void selectThumbnail() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Thumbnail"), 1);
    }
    private void createLibrary() {
        String libraryName = libraryNameEditText.getText().toString().trim();
        String categoryType = categoryTypeEditText.getText().toString().trim();

        if (!libraryName.isEmpty() && !categoryType.isEmpty()) {
            DatabaseReference librariesRef = FirebaseDatabase.getInstance().getReference("libraries");
            String libraryId = librariesRef.push().getKey();

            if (thumbnailUri != null) {
                String extension = getFileExtension(thumbnailUri);
                StorageReference storageRef = FirebaseStorage.getInstance().getReference("thumbnails").child(libraryId + "." + extension);

                // Show overlay and start progress indicator
                overlay.setVisibility(View.VISIBLE);
                progressIndicator.setVisibility(View.VISIBLE);
                progressIndicator.setIndeterminate(true); // Make it indeterminate

                uploadFile(storageRef, thumbnailUri, url -> {
                    Library library = new Library(libraryId, libraryName, categoryType, 0, url);
                    saveLibrary(librariesRef, library);
                });
            } else {
                Library library = new Library(libraryId, libraryName, categoryType, 0, null);
                saveLibrary(librariesRef, library);
            }
        } else {
            Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
        }
    }

private void saveLibrary(DatabaseReference librariesRef, Library library) {
    librariesRef.child(library.getId()).setValue(library)
            .addOnSuccessListener(aVoid -> {
                hideProgress(); // Call method to hide
                Toast.makeText(getActivity(), "Library created", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                hideProgress(); // Call method to hide
                Toast.makeText(getActivity(), "Failed to create library: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
}

    private void hideProgress() {
        overlay.setVisibility(View.GONE); // Hide overlay
        progressIndicator.setVisibility(View.GONE); // Hide progress indicator
    }

    private void uploadFile(StorageReference storageRef, Uri fileUri, OnSuccessCallback callback) {
        storageRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            callback.onSuccess(uri.toString());
                        }).addOnFailureListener(e -> {
                            overlay.setVisibility(View.GONE); // Hide overlay
                            progressIndicator.setVisibility(View.GONE); // Hide progress indicator
                            Toast.makeText(getActivity(), "Failed to get file URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e -> {
                    overlay.setVisibility(View.GONE); // Hide overlay
                    progressIndicator.setVisibility(View.GONE); // Hide progress indicator
                    Toast.makeText(getActivity(), "Failed to upload file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            thumbnailUri = data.getData();
            libraryThumbnailImageView.setImageURI(thumbnailUri); // Update ImageView with selected image
        }
    }

    private void navigateToAddEpisodes() {
        if (isAdded()) {
            DashboardActivity activity = (DashboardActivity) getActivity();
            if (activity != null) {
                activity.loadFragment(new Add_Episodes_Fragment());
            }
        }
    }

    @FunctionalInterface
    interface OnSuccessCallback {
        void onSuccess(String url);
    }
}


