package com.example.mbook.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mbook.R;
import com.example.mbook.adapters.AdminLibraryAdapter;
import com.example.mbook.models.Library;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Content_manage_Fragment extends Fragment {

    private RecyclerView recyclerView;
    private AdminLibraryAdapter adminLibraryAdapter;
    private List<Library> libraryList = new ArrayList<>();
    private List<Library> filteredLibraryList = new ArrayList<>();
    private SearchView searchView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_content_manage_, container, false);
        initializeViews(view);
        loadLibraries();
        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adminLibraryAdapter = new AdminLibraryAdapter(filteredLibraryList, this::showLibraryDetails, this::deleteLibrary);
        recyclerView.setAdapter(adminLibraryAdapter);

        searchView = view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterLibraries(newText);
                return true;
            }
        });
    }

    private void loadLibraries() {
        DatabaseReference librariesRef = FirebaseDatabase.getInstance().getReference("libraries");
        librariesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                libraryList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Library library = dataSnapshot.getValue(Library.class);
                    if (library != null) {
                        library.setId(dataSnapshot.getKey());
                        libraryList.add(library);
                    }
                }
                updateFilteredLibraryList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Failed to load libraries: " + error.getMessage());
            }
        });
    }

    private void updateFilteredLibraryList() {
        filteredLibraryList.clear();
        filteredLibraryList.addAll(libraryList);
        adminLibraryAdapter.notifyDataSetChanged();

        if (filteredLibraryList.isEmpty()) {
            showToast("No libraries found.");
        }
    }

    private void filterLibraries(String query) {
        filteredLibraryList.clear();
        if (query.isEmpty()) {
            filteredLibraryList.addAll(libraryList);
        } else {
            for (Library library : libraryList) {
                if (library.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredLibraryList.add(library);
                }
            }
        }
        adminLibraryAdapter.notifyDataSetChanged();
    }

    private void showLibraryDetails(Library library) {
        Content_update_Fragment fragment = new Content_update_Fragment();
        Bundle args = new Bundle();
        args.putString("libraryId", library.getId());
        fragment.setArguments(args);

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void deleteLibrary(Library library) {
        DatabaseReference libraryRef = FirebaseDatabase.getInstance().getReference("libraries").child(library.getId());
        libraryRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    showToast("Library deleted successfully");
                    loadLibraries();  // Reload libraries
                })
                .addOnFailureListener(e -> showToast("Failed to delete library: " + e.getMessage()));
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
