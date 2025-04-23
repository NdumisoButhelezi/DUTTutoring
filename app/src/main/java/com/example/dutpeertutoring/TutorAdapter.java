package com.example.dutpeertutoring;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class TutorAdapter extends BaseAdapter {
    private final Context context;
    private final List<Tutor> tutorList;

    public TutorAdapter(Context context, List<Tutor> tutorList) {
        this.context = context;
        this.tutorList = tutorList;
    }

    @Override
    public int getCount() {
        return tutorList.size();
    }

    @Override
    public Object getItem(int position) {
        return tutorList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_tutor, parent, false);
        }

        TextView nameTextView = convertView.findViewById(R.id.tutorNameTextView);
        TextView modulesTextView = convertView.findViewById(R.id.tutorModulesTextView);

        Tutor tutor = tutorList.get(position);
        nameTextView.setText(tutor.getName());
        modulesTextView.setText("Modules: " + String.join(", ", tutor.getModules()));

        return convertView;
    }
}