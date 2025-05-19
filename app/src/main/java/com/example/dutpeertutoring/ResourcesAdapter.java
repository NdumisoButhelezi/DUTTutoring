package com.example.dutpeertutoring;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourcesAdapter extends RecyclerView.Adapter<ResourcesAdapter.ViewHolder> {

    private List<Resource> resources;
    private Context context;

    public ResourcesAdapter(List<Resource> resources, Context context) {
        this.resources = resources;
        this.context = context;
    }

    public void updateResources(List<Resource> resources) {
        this.resources = resources;
        notifyDataSetChanged();  // Notify the adapter to refresh the RecyclerView
    }

    public void saveRatingsToFirestore() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        for (Resource resource : resources) {
            // Assuming you're storing ratings in Firestore along with other details
            float rating = resource.getRating();  // You need to ensure the Resource class has a rating field

            // Prepare the data to be saved to Firestore
            Map<String, Object> ratingData = new HashMap<>();
            ratingData.put("resourceId", resource.getId());
            ratingData.put("rating", rating);
            ratingData.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

            firestore.collection("ratings")
                    .add(ratingData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Rating saved successfully!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Failed to save rating: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @NonNull
    @Override
    public ResourcesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_resource, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResourcesAdapter.ViewHolder holder, int position) {
        Resource resource = resources.get(position);
        holder.tvTitle.setText(resource.getTitle());
        holder.tvTutorName.setText("Tutor: " + resource.getTutorName());

        holder.btnDownload.setOnClickListener(v -> {
            String base64EncodedPdf = resource.getPdfData();
            String filename = resource.getTitle() + ".pdf";
            ((StudentResourcesActivity) context).downloadPdf(base64EncodedPdf, filename);
        });

        holder.ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            // Handle rating logic
            resource.setRating(rating);
        });
    }

    @Override
    public int getItemCount() {
        return resources.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvTutorName;
        RatingBar ratingBar;
        Button btnDownload;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvResourceTitle);
            tvTutorName = itemView.findViewById(R.id.tvTutorName);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            btnDownload = itemView.findViewById(R.id.btnDownloadPdf);
        }
    }
}
