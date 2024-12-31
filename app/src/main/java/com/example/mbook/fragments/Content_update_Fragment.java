package com.example.mbook.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.mbook.R;
import com.example.mbook.models.Library;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class Content_update_Fragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private Spinner librarySpinner;
    private EditText libraryNameEditText, categoryTypeEditText;
    private Button saveChangesButton, editLibraryContentsButton, updateThumbnailButton;
    private ImageView libraryThumbnailImageView;
    private ProgressBar progressBar;

    private List<Library> libraryList = new ArrayList<>();
    private String selectedLibraryId;
    private Uri thumbnailUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_content_update_, container, false);

        initializeViews(view);
        setupLibrarySpinner();

        saveChangesButton.setOnClickListener(v -> saveChanges());
        editLibraryContentsButton.setOnClickListener(v -> navigateToEditLibraryContents());
        updateThumbnailButton.setOnClickListener(v -> openImagePicker());

        return view;
    }

    private void initializeViews(View view) {
        librarySpinner = view.findViewById(R.id.library_spinner);
        libraryNameEditText = view.findViewById(R.id.edit_library_name);
        categoryTypeEditText = view.findViewById(R.id.edit_category_type);
        saveChangesButton = view.findViewById(R.id.save_changes_button);
        editLibraryContentsButton = view.findViewById(R.id.edit_library_contents_button);
        updateThumbnailButton = view.findViewById(R.id.update_thumbnail_button);
        libraryThumbnailImageView = view.findViewById(R.id.library_thumbnail);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void setupLibrarySpinner() {
        DatabaseReference librariesRef = FirebaseDatabase.getInstance().getReference("libraries");
        librariesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                libraryList.clear();
                List<String> libraryNames = new ArrayList<>();

                for (DataSnapshot librarySnapshot : dataSnapshot.getChildren()) {
                    Library library = librarySnapshot.getValue(Library.class);
                    if (library != null) {
                        libraryList.add(library);
                        libraryNames.add(library.getName());
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, libraryNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                librarySpinner.setAdapter(adapter);

                librarySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedLibraryId = libraryList.get(position).getId();
                        libraryNameEditText.setText(libraryList.get(position).getName());
                        categoryTypeEditText.setText(libraryList.get(position).getCategory());
                        loadLibraryThumbnail(libraryList.get(position));
                        editLibraryContentsButton.setEnabled(true);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        selectedLibraryId = null;
                        editLibraryContentsButton.setEnabled(false);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast("Failed to load libraries: " + databaseError.getMessage());
            }
        });
    }

    private void loadLibraryThumbnail(Library library) {
        String thumbnailUrl = library.getThumbnail();
        if (thumbnailUrl != null) {
            Glide.with(this).load(thumbnailUrl).into(libraryThumbnailImageView);
        } else {
            libraryThumbnailImageView.setImageResource(R.drawable.placeholder_image);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Thumbnail"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            thumbnailUri = data.getData();
            libraryThumbnailImageView.setImageURI(thumbnailUri);
        }
    }

    private void saveChanges() {
        String updatedName = libraryNameEditText.getText().toString().trim();
        String updatedCategory = categoryTypeEditText.getText().toString().trim();

        if (selectedLibraryId != null) {
            progressBar.setVisibility(View.VISIBLE);
            DatabaseReference librariesRef = FirebaseDatabase.getInstance().getReference("libraries").child(selectedLibraryId);

            if (!updatedName.isEmpty()) {
                librariesRef.child("name").setValue(updatedName);
            }
            if (!updatedCategory.isEmpty()) {
                librariesRef.child("category").setValue(updatedCategory);
            }

            if (thumbnailUri != null) {
                uploadThumbnail();
            } else {
                progressBar.setVisibility(View.GONE);
                showToast("Library updated successfully");
            }
        } else {
            showToast("Please select a library first");
        }
    }

    private void uploadThumbnail() {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("library_thumbnails/" + selectedLibraryId + ".jpg");
        storageRef.putFile(thumbnailUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    DatabaseReference librariesRef = FirebaseDatabase.getInstance().getReference("libraries");
                    librariesRef.child(selectedLibraryId).child("thumbnail").setValue(uri.toString())
                            .addOnSuccessListener(aVoid -> {
                                progressBar.setVisibility(View.GONE);
                                showToast("Thumbnail updated successfully");
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                showToast("Failed to update thumbnail: " + e.getMessage());
                            });
                }))
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    showToast("Failed to upload thumbnail: " + e.getMessage());
                });
    }

    private void navigateToEditLibraryContents() {
        if (selectedLibraryId != null) {
            EditLibraryContentsFragment fragment = new EditLibraryContentsFragment();
            Bundle args = new Bundle();
            args.putString("libraryId", selectedLibraryId);
            fragment.setArguments(args);

            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            showToast("Please select a library first");
        }
    }

    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}


