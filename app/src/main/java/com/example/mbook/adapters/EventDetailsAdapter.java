package com.example.mbook.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mbook.R;
import com.example.mbook.models.EventDetail;

import java.util.List;

public class EventDetailsAdapter extends RecyclerView.Adapter<EventDetailsAdapter.EventViewHolder> {

    private List<EventDetail> eventDetailsList;

    public EventDetailsAdapter(List<EventDetail> eventDetailsList) {
        this.eventDetailsList = eventDetailsList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event_detail, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventDetail eventDetail = eventDetailsList.get(position);
        holder.bind(eventDetail);
    }

    @Override
    public int getItemCount() {
        return eventDetailsList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvEventName;
        TextView tvEventDetails;
        TextView tvEventDate;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventName = itemView.findViewById(R.id.tv_event_name);
            tvEventDetails = itemView.findViewById(R.id.tv_event_details);
            tvEventDate = itemView.findViewById(R.id.tv_event_date);
        }

        public void bind(EventDetail eventDetail) {
            tvEventName.setText(eventDetail.getEventName());
            tvEventDetails.setText(eventDetail.getEventDetails());
            tvEventDate.setText(eventDetail.getEventDate());
        }
    }
}
