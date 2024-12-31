package com.example.mbook.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mbook.R;
import com.example.mbook.adapters.LibraryAdapter;
import com.example.mbook.models.Library;
import com.example.mbook.utils.AdManager; // Make sure you import your AdManager
import android.widget.SearchView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private RecyclerView resultsRecyclerView;
    private LibraryAdapter libraryAdapter;
    private List<Library> libraryList = new ArrayList<>();
    private List<Library> filteredList = new ArrayList<>();
    private SearchView searchInput;  // Updated to SearchView
    private AdManager adManager; // Declare AdManager

    private static final String TAG = "SearchFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Initialize views
        searchInput = view.findViewById(R.id.search_input);
        resultsRecyclerView = view.findViewById(R.id.results_recycler_view);

        // Set up RecyclerView
        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        libraryAdapter = new LibraryAdapter(filteredList, this::showLibraryDetails);
        resultsRecyclerView.setAdapter(libraryAdapter);

        // Initialize AdManager with your ad unit ID
        adManager = new AdManager("ca-app-pub-3940256099942544/1033173712"); // Replace with your ad unit ID
        adManager.loadAd(getContext()); // Load ad

        // Load libraries from Firebase
        loadLibraries();

        // Add text change listener to search input
        searchInput.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterLibraries(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterLibraries(newText);
                return false;
            }
        });

        return view;
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
                // Apply the current search filter after loading data
                filterLibraries(searchInput.getQuery().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "DatabaseError: " + error.getMessage());
                Toast.makeText(getContext(), "Failed to load libraries. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterLibraries(String query) {
        filteredList.clear();
        String lowerCaseQuery = query.toLowerCase();
        for (Library library : libraryList) {
            if (library.getName() != null && library.getName().toLowerCase().contains(lowerCaseQuery)) {
                filteredList.add(library);
            }
        }
        libraryAdapter.notifyDataSetChanged();
    }

    private void showLibraryDetails(Library library) {
        adManager.showAd(getContext(), () -> {
            LibraryDetailsFragment fragment = LibraryDetailsFragment.newInstance(library);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment) // Replace with the container ID in your layout
                    .addToBackStack(null)
                    .commit();
        });
    }
}



//package com.example.mbook;
//
//import android.os.Bundle;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.mbook.adapters.LibraryAdapter;
//import com.example.mbook.models.Library;
//import android.widget.SearchView;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class SearchFragment extends Fragment {
//
//    private RecyclerView resultsRecyclerView;
//    private LibraryAdapter libraryAdapter;
//    private List<Library> libraryList = new ArrayList<>();
//    private List<Library> filteredList = new ArrayList<>();
//    private SearchView searchInput;  // Updated to SearchView
//
//    private static final String TAG = "SearchFragment";
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_search, container, false);
//
//        // Initialize views
//        searchInput = view.findViewById(R.id.search_input);
//        resultsRecyclerView = view.findViewById(R.id.results_recycler_view);
//
//        // Set up RecyclerView
//        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        libraryAdapter = new LibraryAdapter(filteredList, this::showLibraryDetails);
//        resultsRecyclerView.setAdapter(libraryAdapter);
//
//        // Load libraries from Firebase
//        loadLibraries();
//
//        // Add text change listener to search input
//        searchInput.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                filterLibraries(query);
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                filterLibraries(newText);
//                return false;
//            }
//        });
//
//        return view;
//    }
//
//    private void loadLibraries() {
//        DatabaseReference librariesRef = FirebaseDatabase.getInstance().getReference("libraries");
//        librariesRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                libraryList.clear();
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                    Library library = dataSnapshot.getValue(Library.class);
//                    if (library != null) {
//                        library.setId(dataSnapshot.getKey());
//                        libraryList.add(library);
//                    }
//                }
//                // Apply the current search filter after loading data
//                filterLibraries(searchInput.getQuery().toString());
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e(TAG, "DatabaseError: " + error.getMessage());
//                Toast.makeText(getContext(), "Failed to load libraries. Please try again later.", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void filterLibraries(String query) {
//        filteredList.clear();
//        String lowerCaseQuery = query.toLowerCase();
//        for (Library library : libraryList) {
//            if (library.getName() != null && library.getName().toLowerCase().contains(lowerCaseQuery)) {
//                filteredList.add(library);
//            }
//        }
//        libraryAdapter.notifyDataSetChanged();
//    }
//
//    private void showLibraryDetails(Library library) {
//        LibraryDetailsFragment fragment = LibraryDetailsFragment.newInstance(library);
//        getActivity().getSupportFragmentManager().beginTransaction()
//                .replace(R.id.fragment_container, fragment) // Replace with the container ID in your layout
//                .addToBackStack(null)
//                .commit();
//    }
//}
