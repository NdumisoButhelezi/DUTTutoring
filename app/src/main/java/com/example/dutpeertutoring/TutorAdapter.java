package com.example.dutpeertutoring;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TutorAdapter extends RecyclerView.Adapter<TutorAdapter.TutorViewHolder> {

    private List<Tutor> tutors;
    private Context context;

    public TutorAdapter(List<Tutor> tutors, Context context) {
        this.tutors = tutors;
        this.context = context;
    }

    @NonNull
    @Override
    public TutorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tutor_card, parent, false);
        return new TutorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TutorViewHolder holder, int position) {
        Tutor tutor = tutors.get(position);
        holder.name.setText(tutor.getName());
        holder.modules.setText("Modules: " + String.join(", ", tutor.getModules()));
    }

    @Override
    public int getItemCount() {
        return tutors.size();
    }

    public static class TutorViewHolder extends RecyclerView.ViewHolder {
        TextView name, modules;

        public TutorViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tutorName);
            modules = itemView.findViewById(R.id.tutorModules);
        }
    }
}