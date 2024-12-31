package com.example.mbook.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.example.mbook.R;
import com.example.mbook.adapters.SliderAdapter;
import com.example.mbook.models.Library;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Admin_home_Fragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private ViewPager2 viewPagerImages;
    private SliderAdapter sliderAdapter;
    private List<String> imageUrls = new ArrayList<>();

    private CardView manageContentsButton;
    private CardView analyticsButton;
    private CardView usersButton;
    private FragmentManager fragmentManager;

    private Handler handler = new Handler();
    private Runnable runnable;
    private int currentPage = 0;

    public Admin_home_Fragment() {
        // Required empty public constructor
    }

    public static Admin_home_Fragment newInstance(String param1, String param2) {
        Admin_home_Fragment fragment = new Admin_home_Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

@SuppressLint("MissingInflatedId")
@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container,
                         Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_admin_home_, container, false);

    viewPagerImages = view.findViewById(R.id.view_pager_images);

    // Initialize fragmentManager
    fragmentManager = getParentFragmentManager(); // or getChildFragmentManager() if this fragment is a child

    // Initialize the adapter with the imageUrls and click listener
    sliderAdapter = new SliderAdapter(imageUrls, imageUrl -> {
        // Handle image click
//        Toast.makeText(getContext(), "Clicked on: " + imageUrl, Toast.LENGTH_SHORT).show();
    });

    viewPagerImages.setAdapter(sliderAdapter);

    manageContentsButton = view.findViewById(R.id.cardview1);
    analyticsButton = view.findViewById(R.id.cardview2);
    usersButton = view.findViewById(R.id.cardview3);

    manageContentsButton.setOnClickListener(v -> loadFragment(new Content_manage_Fragment()));
    analyticsButton.setOnClickListener(v -> loadFragment(new Admin_Analytics_Fragment()));
    usersButton.setOnClickListener(v -> loadFragment(new Admin_Manage_Fragment()));

    loadImages(); // Method to load images into the adapter
    startAutoSlide(); // Optional: For auto-sliding functionality

    return view;
}

    private void loadImages() {
        DatabaseReference librariesRef = FirebaseDatabase.getInstance().getReference("libraries");

        librariesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Library> allLibraries = new ArrayList<>();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Library library = dataSnapshot.getValue(Library.class);
                    if (library != null) {
                        library.setId(dataSnapshot.getKey());
                        allLibraries.add(library);
                    }
                }

                Collections.sort(allLibraries, (lib1, lib2) -> Integer.compare(lib2.getLikes(), lib1.getLikes()));

                imageUrls.clear();
                for (int i = 0; i < Math.min(10, allLibraries.size()); i++) {
                    Library topLibrary = allLibraries.get(i);
                    if (topLibrary.getThumbnail() != null) {
                        imageUrls.add(topLibrary.getThumbnail());
                    }
                }

                sliderAdapter.updateData(imageUrls);
                startAutoSlide(); // Start auto-sliding after loading images
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAutoSlide(); // Stop auto-sliding when the view is destroyed
    }

    private void stopAutoSlide() {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    void loadFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

}
