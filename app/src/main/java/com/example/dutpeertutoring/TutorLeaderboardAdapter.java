package com.example.dutpeertutoring;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TutorLeaderboardAdapter extends RecyclerView.Adapter<TutorLeaderboardAdapter.TutorViewHolder> {

    private List<Tutor> tutors;
    private Context context;

    public TutorLeaderboardAdapter(List<Tutor> tutors, Context context) {
        this.tutors = tutors;
        this.context = context;
    }

    @NonNull
    @Override
    public TutorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tutor_leaderboard, parent, false);
        return new TutorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TutorViewHolder holder, int position) {
        Tutor tutor = tutors.get(position);
        holder.nameTextView.setText(tutor.getName() + " " + tutor.getSurname());
        holder.ratingTextView.setText("Rating: " + tutorRating(tutor));
        // Optionally, display the rank position
        holder.rankTextView.setText("#" + (position + 1));
    }

    private String tutorRating(Tutor tutor) {
        // Assumes that the tutor document has a "rating" field stored as a number.
        // If the field is missing, display N/A.
        try {
            // Reflection is used here for demonstration.
            // If you add a getRating() method in Tutor, use that instead.
            Object ratingObj = tutor.getClass().getDeclaredField("rating").get(tutor);
            if (ratingObj != null) {
                return String.valueOf(ratingObj);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Field not found or inaccessible.
        }
        return "N/A";
    }

    @Override
    public int getItemCount() {
        return tutors.size();
    }

    public static class TutorViewHolder extends RecyclerView.ViewHolder {
        TextView rankTextView;
        TextView nameTextView;
        TextView ratingTextView;

        public TutorViewHolder(@NonNull View itemView) {
            super(itemView);
            rankTextView = itemView.findViewById(R.id.rankTextView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            ratingTextView = itemView.findViewById(R.id.ratingTextView);
        }
    }
}