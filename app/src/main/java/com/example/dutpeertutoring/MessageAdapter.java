package com.example.dutpeertutoring;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends BaseAdapter {

    private final Context context;
    private final List<Message> messages;
    private final String currentUserId;

    public MessageAdapter(Context context, List<Message> messages, String currentUserId) {
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message message = messages.get(position);

        // Check if the message is sent by the current user or the other user
        boolean isCurrentUser = message.getSenderId().equals(currentUserId);

        // Choose the appropriate layout for the sender or receiver
        if (convertView == null) {
            int layoutId = isCurrentUser ? R.layout.item_message_sent : R.layout.item_message_received;
            convertView = LayoutInflater.from(context).inflate(layoutId, parent, false);
        }

        TextView messageText = convertView.findViewById(R.id.messageText);
        TextView timestamp = convertView.findViewById(R.id.timestamp);

        // Set message text and timestamp
        messageText.setText(message.getText());
        timestamp.setText(formatTimestamp(message.getTimestamp()));

        return convertView;
    }

    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(timestamp);
    }
}