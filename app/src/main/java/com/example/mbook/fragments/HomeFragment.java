package com.example.mbook.fragments;

import android.os.Bundle;
import android.os.Handler;
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
import androidx.viewpager2.widget.ViewPager2;

import com.example.mbook.R;
import com.example.mbook.adapters.LibraryAdapter;
import com.example.mbook.adapters.SliderAdapter;
import com.example.mbook.models.Library;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewHorizontal;
    private RecyclerView recyclerViewVertical;
    private LibraryAdapter libraryAdapterHorizontal;
    private LibraryAdapter libraryAdapterVertical;
    private SliderAdapter sliderAdapter;
    private List<Library> libraryList = new ArrayList<>();
    private List<Library> topLibraries = new ArrayList<>();
    private ChipGroup chipGroupCategories;
    private ViewPager2 viewPagerImages;
    private String selectedCategory = "All"; // Default category

    private Handler handler = new Handler();
    private Runnable runnable;
    private int currentPage = 0;
    private static final String TAG = "HomeFragment";
    private static final String INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712";
    private InterstitialAd mInterstitialAd;
    public static final String TEST_DEVICE_HASHED_ID = "ABCDEF012345";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerViewHorizontal = view.findViewById(R.id.recycler_view);
        recyclerViewVertical = view.findViewById(R.id.recycler_view_vertical);
        viewPagerImages = view.findViewById(R.id.view_pager_images);
        chipGroupCategories = view.findViewById(R.id.chip_group_categories);

        // Set the horizontal RecyclerView layout
        recyclerViewHorizontal.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        libraryAdapterHorizontal = new LibraryAdapter(libraryList, this::onLibraryClicked);
        recyclerViewHorizontal.setAdapter(libraryAdapterHorizontal);

        // Set the vertical RecyclerView layout
        recyclerViewVertical.setLayoutManager(new LinearLayoutManager(getContext()));
        libraryAdapterVertical = new LibraryAdapter(libraryList, this::onLibraryClicked);
        recyclerViewVertical.setAdapter(libraryAdapterVertical);

        sliderAdapter = new SliderAdapter(new ArrayList<>(), this::onSliderImageClicked);
        viewPagerImages.setAdapter(sliderAdapter);

        setupCategoryFilters();
        loadLibraries();
        loadInterstitialAd(); // Load ad here

        return view;
    }

    private void setupCategoryFilters() {
        chipGroupCategories.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chip = group.findViewById(checkedId);
            if (chip != null) {
                selectedCategory = chip.getText().toString();
                Log.d(TAG, "Selected category: " + selectedCategory);
                loadLibraries(); // Reload libraries based on selected category
            }
        });
    }

    private void loadLibraries() {
        DatabaseReference librariesRef = FirebaseDatabase.getInstance().getReference("libraries");

        librariesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                libraryList.clear();
                List<Library> allLibraries = new ArrayList<>();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Library library = dataSnapshot.getValue(Library.class);
                    if (library != null) {
                        library.setId(dataSnapshot.getKey());
                        if (selectedCategory.equals("All") || library.getCategory().equals(selectedCategory)) {
                            libraryList.add(library);
                            allLibraries.add(library);
                        }
                    }
                }

                // Sort libraries by likes in descending order and get top 4
                Collections.sort(allLibraries, Comparator.comparingInt(Library::getLikes).reversed());

                topLibraries.clear();
                if (allLibraries.size() > 4) {
                    topLibraries.addAll(allLibraries.subList(0, 4));
                } else {
                    topLibraries.addAll(allLibraries);
                }

                // Update slider with top 4 libraries
                List<String> imageUrls = new ArrayList<>();
                for (Library lib : topLibraries) {
                    if (lib.getThumbnail() != null) {
                        imageUrls.add(lib.getThumbnail());
                    }
                }
                sliderAdapter.updateData(imageUrls);

                libraryAdapterHorizontal.notifyDataSetChanged();
                libraryAdapterVertical.notifyDataSetChanged();
                sliderAdapter.notifyDataSetChanged();
                viewPagerImages.setAdapter(sliderAdapter);

                startAutoSlide();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load libraries: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startAutoSlide() {
        final int slideInterval = 5000; // 5 seconds

        runnable = new Runnable() {
            @Override
            public void run() {
                if (viewPagerImages != null && sliderAdapter.getItemCount() > 0) {
                    currentPage = (currentPage + 1) % sliderAdapter.getItemCount();
                    viewPagerImages.setCurrentItem(currentPage, true);
                    handler.postDelayed(this, slideInterval);
                }
            }
        };
        handler.postDelayed(runnable, slideInterval);
    }

    private void stopAutoSlide() {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    private void onLibraryClicked(Library library) {
        showInterstitialAd(library);
    }

    private void onSliderImageClicked(String imageUrl) {
        for (Library lib : topLibraries) {
            if (lib.getThumbnail().equals(imageUrl)) {
                showInterstitialAd(lib);
                break;
            }
        }
    }

    private void showInterstitialAd(Library library) {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(getActivity());
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    showLibraryDetails(library);
                    loadInterstitialAd();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    Log.d(TAG, "Ad failed to show: " + adError.getMessage());
                    showLibraryDetails(library);
                }
            });
        } else {
            Log.d(TAG, "The interstitial ad wasn't ready yet.");
            showLibraryDetails(library); // Show details if ad isn't ready
        }
    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(getContext(), INTERSTITIAL_AD_UNIT_ID, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        Log.d(TAG, "Interstitial ad loaded.");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.d(TAG, "Failed to load interstitial ad: " + loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });
    }

    private void showLibraryDetails(Library library) {
        LibraryDetailsFragment fragment = LibraryDetailsFragment.newInstance(library);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment) // Replace with the container ID in your layout
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAutoSlide(); // Stop auto-sliding when the view is destroyed
    }
}
