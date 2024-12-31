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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mbook.R;
import com.example.mbook.models.Episode;
import com.example.mbook.models.Library;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Add_Episodes_Fragment extends Fragment {

    private Spinner librarySpinner;
    private EditText episodeTitleEditText;
    private Button selectAudioFileButton, selectDocumentFileButton, addEpisodeButton;
    private ProgressBar progressBar;
    private View blurOverlay; // Overlay for blur effect
    private Uri audioFileUri, documentFileUri;
    private String selectedLibraryId;
    private Library selectedLibrary;
    private List<Library> libraryList = new ArrayList<>();

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add__episodes_, container, false);

        librarySpinner = view.findViewById(R.id.library_spinner);
        episodeTitleEditText = view.findViewById(R.id.episode_title);
        selectAudioFileButton = view.findViewById(R.id.select_audio_file);
        selectDocumentFileButton = view.findViewById(R.id.select_document_file);
        addEpisodeButton = view.findViewById(R.id.add_episode_button);
        progressBar = view.findViewById(R.id.progress_bar);
        blurOverlay = view.findViewById(R.id.blur_overlay); // Initialize the overlay

        selectAudioFileButton.setOnClickListener(v -> selectAudioFile());
        selectDocumentFileButton.setOnClickListener(v -> selectDocumentFile());
        addEpisodeButton.setOnClickListener(v -> addEpisode());

        setupLibrarySpinner();
        return view;
    }

    private void addEpisode() {
        String title = episodeTitleEditText.getText().toString().trim();

        if (audioFileUri != null && documentFileUri != null && selectedLibraryId != null && !title.isEmpty()) {
            progressBar.setVisibility(View.VISIBLE); // Show progress bar
            blurOverlay.setVisibility(View.VISIBLE); // Show blur overlay

            String episodeId = UUID.randomUUID().toString();
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("episodes");

            String audioExtension = getFileExtension(audioFileUri);
            String documentExtension = getFileExtension(documentFileUri);

            StorageReference audioRef = storageRef.child(episodeId + "." + audioExtension);
            StorageReference docRef = storageRef.child(episodeId + "." + documentExtension);

            uploadFile(audioRef, audioFileUri, audioUrl -> {
                uploadFile(docRef, documentFileUri, docUrl -> {
                    Episode episode = new Episode(title, audioUrl, docUrl, selectedLibrary.getThumbnail());
                    DatabaseReference episodesRef = FirebaseDatabase.getInstance()
                            .getReference("libraries")
                            .child(selectedLibraryId)
                            .child("episodes");

                    episodesRef.child(episodeId).setValue(episode)
                            .addOnSuccessListener(aVoid -> {
                                progressBar.setVisibility(View.GONE); // Hide progress bar
                                blurOverlay.setVisibility(View.GONE); // Hide blur overlay
                                Toast.makeText(getActivity(), "Episode added", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE); // Hide progress bar
                                blurOverlay.setVisibility(View.GONE); // Hide blur overlay
                                Toast.makeText(getActivity(), "Failed to add episode: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                });
            });
        } else {
            Toast.makeText(getActivity(), "Please select audio and document files and enter a title", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadFile(StorageReference storageRef, Uri fileUri, OnSuccessCallback callback) {
        progressBar.setIndeterminate(true); // Use indeterminate mode if you can't calculate progress
        progressBar.setVisibility(View.VISIBLE); // Show progress bar

        storageRef.putFile(fileUri)
                .addOnProgressListener(taskSnapshot -> {
                    // You can also calculate progress if you want
                    int progress = (int) (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progressBar.setProgress(progress); // Update progress bar
                })
                .addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String fileUrl = uri.toString();
                                    callback.onSuccess(fileUrl);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getActivity(), "Failed to get file URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to upload file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE); // Hide progress bar after complete
                    blurOverlay.setVisibility(View.GONE); // Hide blur overlay after complete
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
        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (requestCode == 1) {
                audioFileUri = uri;
            } else if (requestCode == 2) {
                documentFileUri = uri;
            }
        }
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Failed to load libraries: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        librarySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLibrary = libraryList.get(position);
                selectedLibraryId = selectedLibrary.getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedLibrary = null;
                selectedLibraryId = null;
            }
        });
    }

    private void selectAudioFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(Intent.createChooser(intent, "Select Audio"), 1);
    }

    private void selectDocumentFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/plain"); // Only allow .txt files
        startActivityForResult(Intent.createChooser(intent, "Select Document"), 2);
    }

    @FunctionalInterface
    interface OnSuccessCallback {
        void onSuccess(String url);
    }
}
