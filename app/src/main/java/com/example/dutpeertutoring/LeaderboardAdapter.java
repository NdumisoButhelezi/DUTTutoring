package com.example.dutpeertutoring;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
    private List<Tutor> tutors;

    public LeaderboardAdapter(List<Tutor> tutors) {
        this.tutors = tutors;
    }

    public void updateTutors(List<Tutor> tutors) {
        this.tutors = tutors;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tutor tutor = tutors.get(position);
        holder.tvTutorId.setText("Tutor ID: " + tutor.getId());
        holder.tvAverageRating.setText("Average Rating: " + String.format("%.2f", tutor.getAverageRating()));
    }

    @Override
    public int getItemCount() {
        return tutors.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTutorId, tvAverageRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTutorId = itemView.findViewById(R.id.tvTutorId);
            tvAverageRating = itemView.findViewById(R.id.tvAverageRating);
        }
    }
}