package com.example.dutpeertutoring;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class TutorAdapter extends BaseAdapter {

    private List<Tutor> tutors;
    private Context context;

    public TutorAdapter(List<Tutor> tutors, Context context) {
        this.tutors = tutors;
        this.context = context;
    }

    @Override
    public int getCount() {
        return tutors.size();
    }

    @Override
    public Object getItem(int position) {
        return tutors.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_tutor_card, parent, false);
        }

        Tutor tutor = tutors.get(position);

        TextView name = convertView.findViewById(R.id.tutorName);
        TextView modules = convertView.findViewById(R.id.tutorModules);

        name.setText(tutor.getName());
        modules.setText("Modules: " + String.join(", ", tutor.getModules()));

        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        // Return at least 1, since ListView requires at least one view type.
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return tutors.isEmpty();
    }
}