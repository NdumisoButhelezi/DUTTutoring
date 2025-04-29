package com.example.dutpeertutoring;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CardStackTutorAdapter extends RecyclerView.Adapter<CardStackTutorAdapter.TutorViewHolder> {

    private List<Tutor> tutors;
    private Context context;

    public CardStackTutorAdapter(List<Tutor> tutors, Context context) {
        this.tutors = tutors;
        this.context = context;
    }

    @NonNull
    @Override
    public TutorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tutor_card_student, parent, false);
        return new TutorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TutorViewHolder holder, int position) {
        Tutor tutor = tutors.get(position);

        holder.nameTextView.setText(tutor.getName());
        holder.modulesTextView.setText("Modules: " + String.join(", ", tutor.getModules()));
        holder.statusTextView.setText("Status: " + tutor.getStatus());

        String base64Image = tutor.getProfileImageBase64();
        if (base64Image != null && !base64Image.isEmpty()) {
            byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            holder.profileImageView.setImageBitmap(bitmap);
        } else {
            holder.profileImageView.setImageResource(R.drawable.default_profile_image); // Default image
        }
    }

    @Override
    public int getItemCount() {
        return tutors.size();
    }

    public static class TutorViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImageView;
        TextView nameTextView, modulesTextView, statusTextView;

        public TutorViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.tutorProfileImageView);
            nameTextView = itemView.findViewById(R.id.tutorNameTextView);
            modulesTextView = itemView.findViewById(R.id.tutorModulesTextView);
            statusTextView = itemView.findViewById(R.id.tutorStatusTextView);
        }
    }
}