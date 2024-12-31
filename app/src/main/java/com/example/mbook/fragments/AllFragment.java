package com.example.mbook.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mbook.R;
import com.example.mbook.adapters.LibraryAdapter;
import com.example.mbook.adapters.SliderAdapter;
import com.example.mbook.models.Library;
import com.example.mbook.utils.AdManager;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AllFragment extends Fragment {

    private RecyclerView recyclerView;
    private LibraryAdapter libraryAdapter;
    private List<Library> libraryList = new ArrayList<>();
    private List<Library> filteredList = new ArrayList<>();
    private List<String> imageUrls = new ArrayList<>();
    private ViewPager2 viewPagerImages;
    private TextView emptyStateTextView;
    private SearchView searchView;
    private static final String TAG = "AllFragment";
    private AdManager adManager; // Declare AdManager

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        viewPagerImages = view.findViewById(R.id.auto_image_changer);
        emptyStateTextView = view.findViewById(R.id.empty_state_text_view);
        searchView = view.findViewById(R.id.search_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        libraryAdapter = new LibraryAdapter(filteredList, this::showLibraryDetails);
        recyclerView.setAdapter(libraryAdapter);

        // Initialize AdManager with your ad unit ID
        adManager = new AdManager("ca-app-pub-3940256099942544/1033173712"); // Replace with your ad unit ID
        adManager.loadAd(getContext()); // Load ad

        loadLibraries();
        setupSearchView();

        return view;
    }

    private void setupSearchView() {
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

    private void filterLibraries(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(libraryList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Library library : libraryList) {
                if (library.getName().toLowerCase().contains(lowerCaseQuery) ||
                        library.getCategory().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(library);
                }
            }
        }
        updateUI();
    }

    private void loadLibraries() {
        DatabaseReference librariesRef = FirebaseDatabase.getInstance().getReference("libraries");
        librariesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                libraryList.clear();
                imageUrls.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Library library = dataSnapshot.getValue(Library.class);
                    if (library != null) {
                        library.setId(dataSnapshot.getKey());
                        libraryList.add(library);
                        if (library.getThumbnail() != null) {
                            imageUrls.add(library.getThumbnail());
                        }
                    }
                }
                filteredList.addAll(libraryList);
                libraryList.sort((lib1, lib2) -> lib1.getName().compareToIgnoreCase(lib2.getName()));
                updateUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "DatabaseError: " + error.getMessage());
                Toast.makeText(getContext(), "Failed to load libraries. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI() {
        libraryAdapter.notifyDataSetChanged();
        emptyStateTextView.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);

        if (!imageUrls.isEmpty()) {
            SliderAdapter sliderAdapter = new SliderAdapter(imageUrls, imageUrl -> {
                for (Library lib : filteredList) {
                    if (lib.getThumbnail().equals(imageUrl)) {
                        showLibraryDetails(lib);
                        break;
                    }
                }
            });
            viewPagerImages.setAdapter(sliderAdapter);
        } else {
            viewPagerImages.setAdapter(null);
        }
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

