package com.example.mbook.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // Import ImageButton
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mbook.R;
import com.example.mbook.models.Library;

import java.util.List;

public class AdminLibraryAdapter extends RecyclerView.Adapter<AdminLibraryAdapter.AdminLibraryViewHolder> {

    private final List<Library> libraryList;
    private final OnLibraryClickListener onLibraryClickListener;
    private final OnDeleteClickListener onDeleteClickListener;

    public interface OnLibraryClickListener {
        void onLibraryClick(Library library);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Library library);
    }

    public AdminLibraryAdapter(List<Library> libraryList, OnLibraryClickListener onLibraryClickListener, OnDeleteClickListener onDeleteClickListener) {
        this.libraryList = libraryList;
        this.onLibraryClickListener = onLibraryClickListener;
        this.onDeleteClickListener = onDeleteClickListener;
    }

    @NonNull
    @Override
    public AdminLibraryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_library_admin, parent, false);
        return new AdminLibraryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminLibraryViewHolder holder, int position) {
        Library library = libraryList.get(position);

        holder.libraryNameTextView.setText(library.getName());

        // Load thumbnail image using Glide
        Glide.with(holder.itemView.getContext())
                .load(library.getThumbnail()) // Replace with actual URL or resource ID
                .placeholder(R.drawable.placeholder_image) // Placeholder image
                .error(R.drawable.error_image) // Error image
                .into(holder.libraryThumbnailImageView);

        holder.itemView.setOnClickListener(v -> {
            if (onLibraryClickListener != null) {
                onLibraryClickListener.onLibraryClick(library);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (onDeleteClickListener != null) {
                onDeleteClickListener.onDeleteClick(library);
            }
        });
    }

    @Override
    public int getItemCount() {
        return libraryList.size();
    }

    public static class AdminLibraryViewHolder extends RecyclerView.ViewHolder {
        ImageView libraryThumbnailImageView;
        TextView libraryNameTextView;
        ImageButton deleteButton; // Changed to ImageButton

        public AdminLibraryViewHolder(@NonNull View itemView) {
            super(itemView);
            libraryThumbnailImageView = itemView.findViewById(R.id.library_thumbnail_image_view);
            libraryNameTextView = itemView.findViewById(R.id.library_name_text_view);
            deleteButton = itemView.findViewById(R.id.delete_button); // No changes needed here
        }
    }
}
