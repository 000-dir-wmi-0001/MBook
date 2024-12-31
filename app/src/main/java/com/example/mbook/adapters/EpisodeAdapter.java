package com.example.mbook.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mbook.R;
import com.example.mbook.models.Episode;
import com.squareup.picasso.Picasso;

import java.util.List;

public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder> {

    private List<Episode> episodeList;
    private OnEpisodeClickListener onEpisodeClickListener;
    private String libraryThumbnailUrl; // Library thumbnail URL

    public EpisodeAdapter(List<Episode> episodeList, OnEpisodeClickListener listener, String libraryThumbnailUrl) {
        this.episodeList = episodeList;
        this.onEpisodeClickListener = listener;
        this.libraryThumbnailUrl = libraryThumbnailUrl; // Store the library thumbnail URL
    }

    @NonNull
    @Override
    public EpisodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_episode, parent, false);
        return new EpisodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EpisodeViewHolder holder, int position) {
        Episode episode = episodeList.get(position);
        holder.bind(episode);
    }

    @Override
    public int getItemCount() {
        return episodeList.size();
    }

    public interface OnEpisodeClickListener {
        void onEpisodeClick(Episode episode);
    }

    class EpisodeViewHolder extends RecyclerView.ViewHolder {

        private TextView episodeTitle;
        private ImageView episodeThumbnail;

        public EpisodeViewHolder(@NonNull View itemView) {
            super(itemView);
            episodeTitle = itemView.findViewById(R.id.episode_title);
            episodeThumbnail = itemView.findViewById(R.id.episode_thumbnail);
        }

        public void bind(Episode episode) {
            episodeTitle.setText(episode.getTitle());

            // Use the library's thumbnail for all episodes
            if (libraryThumbnailUrl != null && !libraryThumbnailUrl.isEmpty()) {
                Picasso.get().load(libraryThumbnailUrl)
                        .placeholder(R.drawable.placeholder_image) // Placeholder while loading
                        .error(R.drawable.error_image) // Error image if loading fails
                        .into(episodeThumbnail);
            } else {
                episodeThumbnail.setImageResource(R.drawable.placeholder_image); // Fallback if URL is null
            }

            // Set click listeners
            View.OnClickListener listener = v -> {
                if (onEpisodeClickListener != null) {
                    onEpisodeClickListener.onEpisodeClick(episode);
                }
            };

            episodeTitle.setOnClickListener(listener);
            episodeThumbnail.setOnClickListener(listener);
        }
    }
}

