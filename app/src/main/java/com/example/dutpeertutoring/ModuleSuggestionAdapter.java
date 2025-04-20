package com.example.dutpeertutoring;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class ModuleSuggestionAdapter extends BaseAdapter {

    private final Context context;
    private final List<ModuleSuggestion> suggestions;
    private final VoteClickListener approveListener;
    private final VoteClickListener rejectListener;

    public interface VoteClickListener {
        void onVoteClick(ModuleSuggestion suggestion);
    }

    public ModuleSuggestionAdapter(Context context, List<ModuleSuggestion> suggestions, VoteClickListener approveListener, VoteClickListener rejectListener) {
        this.context = context;
        this.suggestions = suggestions;
        this.approveListener = approveListener;
        this.rejectListener = rejectListener;
    }

    @Override
    public int getCount() {
        return suggestions.size();
    }

    @Override
    public Object getItem(int position) {
        return suggestions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_module_suggestion, parent, false);
        }

        ModuleSuggestion suggestion = suggestions.get(position);

        TextView moduleName = convertView.findViewById(R.id.moduleName);
        TextView voteCount = convertView.findViewById(R.id.voteCount);
        Button approveButton = convertView.findViewById(R.id.approveButton);
        Button rejectButton = convertView.findViewById(R.id.rejectButton);

        moduleName.setText(suggestion.getModuleName());
        voteCount.setText("Votes: " + suggestion.getVoteCount());

        approveButton.setOnClickListener(v -> approveListener.onVoteClick(suggestion));
        rejectButton.setOnClickListener(v -> rejectListener.onVoteClick(suggestion));

        return convertView;
    }
}