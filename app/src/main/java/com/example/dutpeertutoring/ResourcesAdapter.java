package com.example.dutpeertutoring;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourcesAdapter extends RecyclerView.Adapter<ResourcesAdapter.ViewHolder> {

    private List<Resource> resources;
    private Map<String, Float> ratingsMap = new HashMap<>(); // Store ratings temporarily

    public ResourcesAdapter(List<Resource> resources) {
        this.resources = resources;
    }

    public void updateResources(List<Resource> resources) {
        this.resources = resources;
        notifyDataSetChanged();
    }

    // Save all ratings to Firestore
    public void saveRatingsToFirestore() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        for (Map.Entry<String, Float> entry : ratingsMap.entrySet()) {
            String resourceId = entry.getKey();
            float rating = entry.getValue();

            firestore.collection("ratings")
                    .add(new Rating(resourceId, rating))
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(null, "Rating saved for resource: " + resourceId, Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(null, "Failed to save rating: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_resource, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Resource resource = resources.get(position);
        holder.tvTitle.setText(resource.getId());
        holder.ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser) {
                ratingsMap.put(resource.getId(), rating); // Save rating temporarily
            }
        });
    }

    @Override
    public int getItemCount() {
        return resources.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        RatingBar ratingBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvResourceTitle);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}