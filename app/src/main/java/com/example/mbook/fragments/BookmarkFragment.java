package com.example.mbook.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView; // Updated import

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mbook.R;
import com.example.mbook.adapters.LibraryAdapter;
import com.example.mbook.models.Library;
import com.example.mbook.utils.AdManager; // Make sure to import your AdManager
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BookmarkFragment extends Fragment {

    private RecyclerView recyclerView;
    private LibraryAdapter libraryAdapter;
    private List<Library> libraryList = new ArrayList<>();
    private List<Library> filteredList = new ArrayList<>();
    private SearchView searchEditText; // Change to SearchView
    private AdManager adManager; // Declare AdManager

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookmark, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recycler_bookmark);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up the adapter
        libraryAdapter = new LibraryAdapter(filteredList, this::showLibraryDetails);
        recyclerView.setAdapter(libraryAdapter);

        // Initialize and set up search functionality
        searchEditText = view.findViewById(R.id.search_edit_text); // No change needed here
        searchEditText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterLibraries(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterLibraries(newText);
                return true;
            }
        });

        // Initialize AdManager with your ad unit ID
        adManager = new AdManager("ca-app-pub-3940256099942544/1033173712"); // Replace with your ad unit ID
        adManager.loadAd(getContext()); // Load ad

        // Load bookmarked libraries
        loadBookmarkedLibraries();

        return view;
    }

    private void loadBookmarkedLibraries() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userBookmarksRef = FirebaseDatabase.getInstance().getReference("bookmarks").child(userId);

        userBookmarksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                libraryList.clear();
                for (DataSnapshot bookmarkSnapshot : snapshot.getChildren()) {
                    String libraryId = bookmarkSnapshot.getKey();
                    DatabaseReference libraryRef = FirebaseDatabase.getInstance().getReference("libraries").child(libraryId);

                    libraryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot librarySnapshot) {
                            Library library = librarySnapshot.getValue(Library.class);
                            if (library != null) {
                                library.setId(libraryId);
                                libraryList.add(library);
                                filterLibraries(searchEditText.getQuery().toString()); // Updated to use getQuery()
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle possible errors.
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors.
            }
        });
    }

    private void filterLibraries(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(libraryList);
        } else {
            String lowerCaseQuery = query.toLowerCase(Locale.ROOT);
            for (Library library : libraryList) {
                if (library.getName().toLowerCase(Locale.ROOT).contains(lowerCaseQuery)) {
                    filteredList.add(library);
                }
            }
        }
        libraryAdapter.notifyDataSetChanged();
    }

    private void showLibraryDetails(Library library) {
        adManager.showAd(getContext(), () -> {
            LibraryDetailsFragment fragment = LibraryDetailsFragment.newInstance(library);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        });
    }
}

