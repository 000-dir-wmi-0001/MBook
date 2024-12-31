package com.example.mbook.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mbook.R;
import com.example.mbook.models.Library;
import com.squareup.picasso.Picasso;

import java.util.List;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.ViewHolder> {

    private final List<Library> libraryList;
    private final OnLibraryClickListener onLibraryClickListener;

    public interface OnLibraryClickListener {
        void onLibraryClick(Library library);
    }

    public LibraryAdapter(List<Library> libraryList, OnLibraryClickListener listener) {
        this.libraryList = libraryList;
        this.onLibraryClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_library, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Library library = libraryList.get(position);
        holder.bind(library, onLibraryClickListener);
    }


    @Override
    public int getItemCount() {
        return libraryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView libraryThumbnailImageView;
        private final TextView libraryNameTextView,libraryLikesTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            libraryThumbnailImageView = itemView.findViewById(R.id.library_thumbnail_image_view);
            libraryNameTextView = itemView.findViewById(R.id.library_name_text_view);
            libraryLikesTextView = itemView.findViewById(R.id.likes_count_text_view); // Initialize
        }

        public void bind(final Library library, final OnLibraryClickListener listener) {
            libraryNameTextView.setText(library.getName());

            // Load the thumbnail using Picasso
            String thumbnailUrl = library.getThumbnail(); // Ensure getThumbnail() is defined in Library
            if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
                Picasso.get().load(thumbnailUrl)
                        .placeholder(R.drawable.placeholder_image) // Optional placeholder
                        .error(R.drawable.error_image) // Optional error image
                        .into(libraryThumbnailImageView);
            } else {
                libraryThumbnailImageView.setImageResource(R.drawable.placeholder_image); // Fallback image
            }

            if (library.getLikes() > 0) {
                libraryLikesTextView.setText("Likes: " + library.getLikes());
                libraryLikesTextView.setVisibility(View.VISIBLE);
            } else {
                libraryLikesTextView.setVisibility(View.GONE); // Hide if no likes
            }
            itemView.setOnClickListener(v -> listener.onLibraryClick(library));
        }
    }
}
