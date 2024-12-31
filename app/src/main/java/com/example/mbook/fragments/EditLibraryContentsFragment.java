package com.example.mbook.fragments;

import android.app.AlertDialog;
import android.app.Activity;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.mbook.R;
import com.example.mbook.models.Episode;
import com.example.mbook.models.Library;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EditLibraryContentsFragment extends Fragment {

    private static final int REQUEST_AUDIO = 1;

    private Spinner librarySpinner;
    private ListView episodesListView;
    private ImageView libraryThumbnail;
    private TextView libraryName;
    private ProgressBar progressBar;

    private List<Library> libraryList = new ArrayList<>();
    private HashMap<String, Episode> episodeMap = new HashMap<>();
    private String selectedLibraryId;
    private Uri newAudioFileUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_library_contents, container, false);
        initializeViews(view);
        setupLibrarySpinner();
        return view;
    }

    private void initializeViews(View view) {
        librarySpinner = view.findViewById(R.id.library_spinner);
        episodesListView = view.findViewById(R.id.episodes_list_view);
        libraryThumbnail = view.findViewById(R.id.library_thumbnail);
        libraryName = view.findViewById(R.id.library_name);
        progressBar = view.findViewById(R.id.progress_bar);
        ImageButton back = view.findViewById(R.id.back_button);

        back.setOnClickListener(v -> requireActivity().onBackPressed());
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
                librarySpinner.setOnItemSelectedListener(new LibrarySelectedListener());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast("Failed to load libraries: " + databaseError.getMessage());
            }
        });
    }

    private class LibrarySelectedListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            selectedLibraryId = libraryList.get(position).getId();
            libraryName.setText(libraryList.get(position).getName());
            Glide.with(getContext()).load(libraryList.get(position).getThumbnail()).into(libraryThumbnail);
            loadEpisodes(selectedLibraryId);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            selectedLibraryId = null;
        }
    }

    private void loadEpisodes(String libraryId) {
        DatabaseReference episodesRef = FirebaseDatabase.getInstance().getReference("libraries").child(libraryId).child("episodes");
        episodesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                episodeMap.clear();
                List<String> episodeTitles = new ArrayList<>();

                for (DataSnapshot episodeSnapshot : dataSnapshot.getChildren()) {
                    Episode episode = episodeSnapshot.getValue(Episode.class);
                    if (episode != null) {
                        episodeMap.put(episodeSnapshot.getKey(), episode);
                        episodeTitles.add(episode.getTitle());
                    }
                }

                ArrayAdapter<String> episodeAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, episodeTitles);
                episodesListView.setAdapter(episodeAdapter);
                episodesListView.setOnItemClickListener((parent, view, position, id) -> {
                    String episodeKey = (String) episodeMap.keySet().toArray()[position];
                    showUpdateDialog(episodeMap.get(episodeKey), episodeKey);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast("Failed to load episodes: " + databaseError.getMessage());
            }
        });
    }

    private void showUpdateDialog(Episode episode, String episodeKey) {
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edit_episode, null);
        EditText episodeTitleEditText = dialogView.findViewById(R.id.episode_title);
        Button selectAudioFileButton = dialogView.findViewById(R.id.select_audio_file);
        Button updateButton = dialogView.findViewById(R.id.update_episode_button);
        Button deleteButton = dialogView.findViewById(R.id.delete_button);

        episodeTitleEditText.setText(episode.getTitle());

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(dialogView)
                .create();

        selectAudioFileButton.setOnClickListener(v -> selectNewAudioFile());
        updateButton.setOnClickListener(v -> handleUpdateEpisode(episodeKey, episodeTitleEditText, dialog));
        deleteButton.setOnClickListener(v -> handleDeleteEpisode(episodeKey, dialog));

        dialog.show();
    }

    private void handleUpdateEpisode(String episodeKey, EditText episodeTitleEditText, AlertDialog dialog) {
        String newTitle = episodeTitleEditText.getText().toString();
        if (newTitle.isEmpty()) {
            showToast("Title cannot be empty");
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        updateEpisode(episodeKey, newTitle, dialog);
    }

    private void handleDeleteEpisode(String episodeKey, AlertDialog dialog) {
        progressBar.setVisibility(View.VISIBLE);
        deleteEpisode(episodeKey, dialog);
    }

    private void updateEpisode(String episodeKey, String newTitle, AlertDialog dialog) {
        DatabaseReference episodeRef = FirebaseDatabase.getInstance().getReference("libraries")
                .child(selectedLibraryId).child("episodes").child(episodeKey);

        episodeRef.child("title").setValue(newTitle)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    showToast("Episode updated successfully");
                    loadEpisodes(selectedLibraryId);
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    showToast("Failed to update episode: " + e.getMessage());
                });
    }

    private void deleteEpisode(String episodeKey, AlertDialog dialog) {
        DatabaseReference episodeRef = FirebaseDatabase.getInstance().getReference("libraries")
                .child(selectedLibraryId).child("episodes").child(episodeKey);

        episodeRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    showToast("Episode deleted successfully");
                    loadEpisodes(selectedLibraryId);
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    showToast("Failed to delete episode: " + e.getMessage());
                });
    }

    private void selectNewAudioFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(Intent.createChooser(intent, "Select Audio File"), REQUEST_AUDIO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null && requestCode == REQUEST_AUDIO) {
            newAudioFileUri = data.getData();
            // Handle the new audio file URI (upload if necessary)
        }
    }

    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}
