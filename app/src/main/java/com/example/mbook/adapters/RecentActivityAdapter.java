package com.example.mbook.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mbook.R;
import com.example.mbook.models.Episode;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.EpisodeViewHolder> implements Filterable {

    private List<Episode> episodeList;
    private final List<Episode> episodeListFull; // To keep the original list for filtering

    public RecentActivityAdapter(List<Episode> episodeList) {
        this.episodeList = new ArrayList<>(episodeList); // Create a new list to avoid modifying the original list
        this.episodeListFull = new ArrayList<>(episodeList); // Copy original list
    }

    @NonNull
    @Override
    public EpisodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_episode, parent, false);
        return new EpisodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EpisodeViewHolder holder, int position) {
        Episode currentEpisode = episodeList.get(position);
        holder.titleTextView.setText(currentEpisode.getTitle());
        Picasso.get().load(currentEpisode.getThumbnailUrl()).into(holder.thumbnailImageView); // Load thumbnail image
    }

    @Override
    public int getItemCount() {
        return episodeList.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Episode> filteredList = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(episodeListFull); // No filter
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (Episode episode : episodeListFull) {
                        if (episode.getTitle().toLowerCase().contains(filterPattern)) {
                            filteredList.add(episode);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                // Use a safe cast with generics
                @SuppressWarnings("unchecked")
                List<Episode> filteredEpisodes = (List<Episode>) results.values;
                episodeList.clear();
                if (filteredEpisodes != null) {
                    episodeList.addAll(filteredEpisodes);
                }
                notifyDataSetChanged();
            }
        };
    }

    static class EpisodeViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        ImageView thumbnailImageView;

        EpisodeViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.episode_title);
            thumbnailImageView = itemView.findViewById(R.id.episode_thumbnail);
        }
    }
}
