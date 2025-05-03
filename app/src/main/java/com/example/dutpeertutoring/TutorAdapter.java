package com.example.dutpeertutoring;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class TutorAdapter extends RecyclerView.Adapter<TutorAdapter.TutorViewHolder> {

    private List<Tutor> tutors;
    private Context context;
    private OnTutorActionListener onTutorActionListener;

    public TutorAdapter(List<Tutor> tutors, Context context, OnTutorActionListener onTutorActionListener) {
        this.tutors = tutors;
        this.context = context;
        this.onTutorActionListener = onTutorActionListener;
    }

    @NonNull
    @Override
    public TutorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tutor_card_admin, parent, false);
        return new TutorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TutorViewHolder holder, int position) {
        Tutor tutor = tutors.get(position);

        // Set the tutor's name
        holder.nameTextView.setText(tutor.getName() != null ? tutor.getName() : "Unknown Tutor");

        // Set the modules
        List<String> modules = tutor.getModules();
        if (modules != null && !modules.isEmpty()) {
            holder.modulesTextView.setText("Modules: " + String.join(", ", modules));
        } else {
            holder.modulesTextView.setText("Modules: No modules available");
        }

        // Load the profile image using Glide
        String base64Image = tutor.getProfileImageBase64();
        if (base64Image != null && !base64Image.isEmpty()) {
            byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
            Glide.with(context)
                    .asBitmap()
                    .load(decodedBytes)
                    .placeholder(R.drawable.default_profile_image)
                    .into(holder.profileImageView);
        } else {
            holder.profileImageView.setImageResource(R.drawable.default_profile_image);
        }

        // Set click listeners for buttons
        holder.approveButton.setOnClickListener(v -> onTutorActionListener.onApproveTutor(tutor));
        holder.rejectButton.setOnClickListener(v -> onTutorActionListener.onRejectTutor(tutor));
    }

    @Override
    public int getItemCount() {
        return tutors.size();
    }

    public static class TutorViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImageView;
        TextView nameTextView, modulesTextView;
        Button approveButton, rejectButton;

        public TutorViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.tutorProfileImageView);
            nameTextView = itemView.findViewById(R.id.tutorNameTextView);
            modulesTextView = itemView.findViewById(R.id.tutorModulesTextView);
            approveButton = itemView.findViewById(R.id.approveButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
        }
    }

    public interface OnTutorActionListener {
        void onApproveTutor(Tutor tutor);
        void onRejectTutor(Tutor tutor);
    }
}