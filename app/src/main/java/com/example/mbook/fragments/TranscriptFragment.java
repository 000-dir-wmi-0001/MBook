package com.example.mbook.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mbook.R;
import com.squareup.picasso.Picasso;

public class TranscriptFragment extends Fragment {

    private static final String ARG_TRANSCRIPT_URL = "transcript_url";
    private static final String ARG_EPISODE_TITLE = "episode_title";
    private static final String ARG_LIBRARY_CATEGORY = "library_category";
    private static final String ARG_THUMBNAIL_URL = "thumbnail_url";

    private String transcriptUrl;
    private WebView webView;
    private ProgressBar progressBar;
    private ImageButton btnBack;
    private ImageButton btnIncreaseTextSize;
    private ImageButton btnDecreaseTextSize;
    private TextView textTitle;
    private TextView textEpisodeTitle;
    private TextView textLibraryCategory;
    private String thumbnailUrl;
    private float currentTextSize = 16f; // Default text size in sp
    private final float textSizeIncrement = 2f; // Amount to increase or decrease text size
    private String episodeTitle;
    private String libraryCategory;
    private ImageView thumbnailImageView;

    public static TranscriptFragment newInstance(Bundle args) {
        TranscriptFragment fragment = new TranscriptFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            transcriptUrl = getArguments().getString(ARG_TRANSCRIPT_URL);
            episodeTitle = getArguments().getString(ARG_EPISODE_TITLE);
            libraryCategory = getArguments().getString(ARG_LIBRARY_CATEGORY);
            thumbnailUrl = getArguments().getString(ARG_THUMBNAIL_URL);
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "WrongViewCast"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transcript, container, false);

        initViews(view);
        setTextValues();
        setListeners();

        if (transcriptUrl != null) {
            setupWebView();
            webView.loadUrl(transcriptUrl);
        } else {
            Toast.makeText(requireContext(), "Transcript URL is not available", Toast.LENGTH_SHORT).show();
        }

        loadThumbnail();
        return view;
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btn_back_transcript);
        progressBar = view.findViewById(R.id.progress_bar);
        webView = view.findViewById(R.id.webview);
        btnIncreaseTextSize = view.findViewById(R.id.btn_increase_text_size);
        btnDecreaseTextSize = view.findViewById(R.id.btn_decrease_text_size);
        textTitle = view.findViewById(R.id.text_title_transcript);
        textEpisodeTitle = view.findViewById(R.id.text_episode_title);
        textLibraryCategory = view.findViewById(R.id.text_library_category);
        thumbnailImageView = view.findViewById(R.id.image_episode_thumbnail);
    }

    private void setTextValues() {
        textTitle.setText("Transcript");
        textEpisodeTitle.setText(episodeTitle);
        textLibraryCategory.setText(libraryCategory);
    }

    private void loadThumbnail() {
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            Picasso.get().load(thumbnailUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(thumbnailImageView);
        }
    }

    private void setListeners() {
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
        btnIncreaseTextSize.setOnClickListener(v -> adjustTextSize(true));
        btnDecreaseTextSize.setOnClickListener(v -> adjustTextSize(false));
    }

    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Failed to load content", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void adjustTextSize(boolean increase) {
        currentTextSize = Math.max(textSizeIncrement, currentTextSize + (increase ? textSizeIncrement : -textSizeIncrement));

        String js = "javascript:(function() {" +
                "document.body.style.fontSize = '" + currentTextSize + "px';" +
                "})()";
        webView.evaluateJavascript(js, null);
    }
}
