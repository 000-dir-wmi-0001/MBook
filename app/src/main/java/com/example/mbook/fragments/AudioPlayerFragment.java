package com.example.mbook.fragments;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mbook.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class AudioPlayerFragment extends Fragment {

    private static final String TAG = "AudioPlayerFragment";

    private String audioUrl, audioTitle, libraryCategory, thumbnailUrl;
    private MediaPlayer mediaPlayer;

    private ImageView albumArtImageView;
    private TextView trackTitleTextView, libraryCategoryTextView, currentPositionTextView, totalDurationTextView;
    private LinearProgressIndicator loadingIndicator, playbackIndicator;
    private ImageButton playButton, pauseButton, backButton, replayButton;

    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            audioUrl = getArguments().getString("audio_url");
            audioTitle = getArguments().getString("audio_title");
            thumbnailUrl = getArguments().getString("thumbnail_url");
            libraryCategory = getArguments().getString("library_category");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audio_player, container, false);
        initializeViews(view);
        setTrackInfo();
        loadThumbnail();
        setButtonListeners();
        return view;
    }

    private void initializeViews(View view) {
        albumArtImageView = view.findViewById(R.id.library_thumbanil_image);
        trackTitleTextView = view.findViewById(R.id.track_title);
        libraryCategoryTextView = view.findViewById(R.id.libary_category);
        currentPositionTextView = view.findViewById(R.id.current_position);
        totalDurationTextView = view.findViewById(R.id.total_duration);
        loadingIndicator = view.findViewById(R.id.loading_indicator);
        playbackIndicator = view.findViewById(R.id.playback_indicator);
        playButton = view.findViewById(R.id.play_button);
        pauseButton = view.findViewById(R.id.pause_button);
        backButton = view.findViewById(R.id.back_button);
        replayButton = view.findViewById(R.id.replay_button);
    }

    private void setTrackInfo() {
        trackTitleTextView.setText(audioTitle);
        libraryCategoryTextView.setText(libraryCategory != null ? libraryCategory : "Unknown Category");
    }

    private void loadThumbnail() {
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            Picasso.get()
                    .load(thumbnailUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(albumArtImageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Thumbnail loaded successfully.");
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e(TAG, "Error loading thumbnail: " + e.getMessage());
                            albumArtImageView.setImageResource(R.drawable.placeholder_image);
                        }
                    });
        } else {
            albumArtImageView.setImageResource(R.drawable.placeholder_image);
        }
    }

    private void setButtonListeners() {
        playButton.setOnClickListener(v -> playAudio());
        pauseButton.setOnClickListener(v -> pauseAudio());
        backButton.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        replayButton.setOnClickListener(v -> replayAudio());
    }

    private void playAudio() {
        if (audioUrl == null || audioUrl.isEmpty()) {
            Log.w(TAG, "Audio URL is null or empty.");
            return;
        }

        if (mediaPlayer == null) {
            initializeMediaPlayer();
        } else if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            updatePlaybackProgress();
        }
    }

    private void initializeMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepareAsync();
            loadingIndicator.setVisibility(View.VISIBLE);
            loadingIndicator.setIndeterminate(true);

            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                loadingIndicator.setVisibility(View.GONE);
                totalDurationTextView.setText(formatDuration(mp.getDuration()));
                playbackIndicator.setVisibility(View.VISIBLE);
                updatePlaybackProgress();
            });
        } catch (IOException e) {
            Log.e(TAG, "Error preparing MediaPlayer: " + e.getMessage());
        }
    }

    private void pauseAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playbackIndicator.setVisibility(View.GONE);
        }
    }

    private void updatePlaybackProgress() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            int currentPosition = mediaPlayer.getCurrentPosition();
            currentPositionTextView.setText(formatDuration(currentPosition));
            playbackIndicator.setProgress((int) ((currentPosition / (float) mediaPlayer.getDuration()) * 100));
            handler.postDelayed(this::updatePlaybackProgress, 1000);
        }
    }

    private void replayAudio() {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(0);
            playAudio();
        }
    }

    private String formatDuration(int duration) {
        int minutes = (duration / 1000) / 60;
        int seconds = (duration / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacksAndMessages(null);
    }
}

