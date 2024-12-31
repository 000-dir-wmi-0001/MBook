
package com.example.mbook.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mbook.R;
import com.example.mbook.adapters.EpisodeAdapter;
import com.example.mbook.models.Episode;
import com.example.mbook.models.Library;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.FullScreenContentCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LibraryDetailsFragment extends Fragment {

    private static final String TAG = "LibraryDetailsFragment";
    private InterstitialAd mInterstitialAd;

    private Library library;
    private ImageView libraryThumbnail;
    private TextView libraryNameTextView;
    private TextView likesTextView;
    private TextView libraryCategoryTextView;
    private ImageButton likeButton;
    private ImageButton bookmarkButton;
    private ImageButton backButton;
    private RecyclerView recyclerViewEpisodes;
    private EpisodeAdapter episodeAdapter;
    private List<Episode> episodeList = new ArrayList<>();
    private boolean isBookmarked;
    private boolean isLiked;

    public static LibraryDetailsFragment newInstance(Library library) {
        LibraryDetailsFragment fragment = new LibraryDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable("library", library);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            library = getArguments().getParcelable("library");
        }
        loadInterstitialAd();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library_details, container, false);
        initializeViews(view);
        setupRecyclerView();

        if (library != null) {
            populateLibraryDetails();
            setClickListeners();
            loadEpisodes();
            checkUserReaction();
            checkIfBookmarked();
        } else {
            Toast.makeText(getContext(), "Library data is missing.", Toast.LENGTH_SHORT).show();
        }
        return view;
    }

    private void initializeViews(View view) {
        libraryThumbnail = view.findViewById(R.id.library_thumbnail);
        libraryNameTextView = view.findViewById(R.id.library_name);
        libraryCategoryTextView = view.findViewById(R.id.library_category_text_view);
        likesTextView = view.findViewById(R.id.likes_count);
        likeButton = view.findViewById(R.id.like_button);
        bookmarkButton = view.findViewById(R.id.bookmark_button);
        backButton = view.findViewById(R.id.back_button);
        recyclerViewEpisodes = view.findViewById(R.id.recycler_view_episodes);
    }

    private void setupRecyclerView() {
        recyclerViewEpisodes.setLayoutManager(new LinearLayoutManager(getContext()));
        episodeAdapter = new EpisodeAdapter(episodeList, this::showPlayOrReadDialog, library.getThumbnail());
        recyclerViewEpisodes.setAdapter(episodeAdapter);
    }

    private void populateLibraryDetails() {
        libraryNameTextView.setText(library.getName());
        libraryCategoryTextView.setText(library.getCategory());
        loadThumbnail();
        loadLikeCounts();
    }

    private void setClickListeners() {
        likeButton.setOnClickListener(v -> handleLike());
        bookmarkButton.setOnClickListener(v -> toggleBookmark());
        backButton.setOnClickListener(v -> {
            if (getFragmentManager() != null) {
                getFragmentManager().popBackStack();
            }
        });
    }

    private void loadThumbnail() {
        String thumbnailUrl = library.getThumbnail();
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            Picasso.get().load(thumbnailUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(libraryThumbnail);
        } else {
            libraryThumbnail.setImageResource(R.drawable.placeholder_image);
        }
    }

    private void loadLikeCounts() {
        DatabaseReference libraryRef = FirebaseDatabase.getInstance().getReference("libraries").child(library.getId());
        libraryRef.child("likes").addListenerForSingleValueEvent(new CountValueEventListener(likesTextView, "Likes: "));
    }

    private void handleLike() {
        if (library == null) return;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userReactionRef = FirebaseDatabase.getInstance().getReference("user_reactions")
                .child(userId)
                .child(library.getId());

        DatabaseReference libraryRef = FirebaseDatabase.getInstance().getReference("libraries").child(library.getId());

        userReactionRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Boolean liked = mutableData.child("liked").getValue(Boolean.class);
                if (liked == null) liked = false;
                mutableData.child("liked").setValue(!liked);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    Log.e(TAG, "Transaction failed: " + error.getMessage());
                    return;
                }
                Boolean newLikedStatus = currentData.child("liked").getValue(Boolean.class);
                updateLikeCount(libraryRef, newLikedStatus);
                updateLikeUI(newLikedStatus);
            }
        });
    }

    private void updateLikeCount(DatabaseReference libraryRef, Boolean newLikedStatus) {
        libraryRef.child("likes").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                Long likes = mutableData.getValue(Long.class);
                if (likes == null) likes = 0L;
                if (newLikedStatus != null) {
                    mutableData.setValue(newLikedStatus ? likes + 1 : (likes > 0 ? likes - 1 : 0));
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    Log.e(TAG, "Transaction failed: " + error.getMessage());
                    return;
                }
                long updatedLikes = currentData.getValue(Long.class) != null ? currentData.getValue(Long.class) : 0;
                likesTextView.setText(String.valueOf(updatedLikes));
            }
        });
    }

    private void updateLikeUI(Boolean liked) {
        if (liked != null) {
            likeButton.setImageResource(liked ? R.drawable.heart_ : R.drawable.heart_border);
        }
    }

    private void toggleBookmark() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference bookmarkRef = FirebaseDatabase.getInstance().getReference("bookmarks")
                .child(userId)
                .child(library.getId());

        bookmarkRef.setValue(isBookmarked ? null : true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                isBookmarked = !isBookmarked;
                bookmarkButton.setImageResource(isBookmarked ? R.drawable.ic_bookmarked : R.drawable.ic_bookmark);
                Toast.makeText(getContext(), isBookmarked ? "Bookmarked!" : "Removed from bookmarks!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to update bookmark.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfBookmarked() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference bookmarkRef = FirebaseDatabase.getInstance().getReference("bookmarks")
                .child(userId)
                .child(library.getId());

        bookmarkRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                isBookmarked = dataSnapshot.exists();
                bookmarkButton.setImageResource(isBookmarked ? R.drawable.ic_bookmarked : R.drawable.ic_bookmark);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to check bookmarks.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadEpisodes() {
        DatabaseReference episodesRef = FirebaseDatabase.getInstance()
                .getReference("libraries")
                .child(library.getId())
                .child("episodes");

        episodesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                episodeList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Episode episode = snapshot.getValue(Episode.class);
                    if (episode != null) {
                        episodeList.add(episode);
                    }
                }

                // Sort the episodeList by title
                Collections.sort(episodeList, (e1, e2) -> e1.getTitle().compareToIgnoreCase(e2.getTitle()));

                // Notify the adapter about the data change
                episodeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load episodes.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void checkUserReaction() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userReactionRef = FirebaseDatabase.getInstance().getReference("user_reactions")
                .child(userId)
                .child(library.getId());

        userReactionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                isLiked = dataSnapshot.child("liked").getValue(Boolean.class) != null && dataSnapshot.child("liked").getValue(Boolean.class);
                likeButton.setImageResource(isLiked ? R.drawable.heart_ : R.drawable.heart_border);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to check reactions.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(requireContext(), "YOUR_AD_UNIT_ID", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        Log.d(TAG, "Ad loaded successfully.");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        mInterstitialAd = null;
                        Log.d(TAG, "Ad failed to load: " + adError.getMessage());
                    }
                });
    }

    private void showPlayOrReadDialog(Episode episode) {
        showInterstitialAd(episode);
    }

    private void showInterstitialAd(Episode episode) {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(requireActivity());
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad was dismissed.");
                    loadInterstitialAd(); // Load another ad
                    showDialog(episode);
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    mInterstitialAd = null;
                }
            });
        } else {
            Log.d(TAG, "The interstitial ad wasn't ready yet.");
            showDialog(episode);
        }
    }

    private void showDialog(Episode episode) {
        new AlertDialog.Builder(requireContext())
                .setTitle(episode.getTitle())
                .setMessage("Would you like to play this episode or read the transcript?")
                .setPositiveButton("Play", (dialog, which) -> openAudioPlayerFragment(episode))
                .setNegativeButton("Read", (dialog, which) -> openTranscript(episode))
                .create()
                .show();
    }

    private void openAudioPlayerFragment(Episode episode) {
        Bundle args = new Bundle();
        args.putString("audio_url", episode.getAudioUrl());
        args.putString("audio_title", episode.getTitle());
        args.putString("thumbnail_url", library.getThumbnail());
        args.putString("library_category", library.getCategory());

        AudioPlayerFragment audioPlayerFragment = new AudioPlayerFragment();
        audioPlayerFragment.setArguments(args);

        if (getFragmentManager() != null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, audioPlayerFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }


    private void openTranscript(Episode episode) {
        Bundle args = new Bundle();
        args.putString("transcript_url", episode.getTextFile());
        args.putString("episode_title", episode.getTitle());
        args.putString("library_category", library.getCategory());
        args.putString("thumbnail_url", library.getThumbnail()); // Assuming there's a method for this

        TranscriptFragment transcriptFragment = TranscriptFragment.newInstance((args));
        if (getFragmentManager() != null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, transcriptFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private static class CountValueEventListener implements ValueEventListener {
        private final TextView textView;
        private final String label;

        CountValueEventListener(TextView textView, String label) {
            this.textView = textView;
            this.label = label;
        }

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            Long count = dataSnapshot.getValue(Long.class);
            textView.setText(label + (count != null ? count : 0));
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.e(TAG, "Failed to read data: " + databaseError.getMessage());
        }
    }
}
