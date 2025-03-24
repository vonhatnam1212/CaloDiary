package com.example.calodiary.chat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calodiary.R;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private static final String TAG = "ChatAdapter";
    private List<ChatMessage> chatMessages;

    public ChatAdapter(List<ChatMessage> chatMessages) {
        // Ensure the list is never null
        this.chatMessages = (chatMessages != null) ? chatMessages : new ArrayList<>();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        try {
            if (viewType == 0) { // User message
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_message, parent, false);
            } else { // AI message
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ai_message, parent, false);
            }
            return new ChatViewHolder(view);
        } catch (Exception e) {
            Log.e(TAG, "Error inflating view for viewType: " + viewType, e);
            // Fallback to a basic view to avoid crashing
            view = new TextView(parent.getContext());
            ((TextView) view).setText("Error loading message");
            return new ChatViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        if (position < 0 || position >= chatMessages.size()) {
            Log.e(TAG, "Invalid position: " + position);
            holder.messageTextView.setText("Error: Invalid message position");
            return;
        }

        ChatMessage chatMessage = chatMessages.get(position);
        if (chatMessage == null) {
            Log.w(TAG, "Null ChatMessage at position: " + position);
            holder.messageTextView.setText("Error: Message not found");
            return;
        }

        String message = chatMessage.getMessage();
        holder.messageTextView.setText(message != null ? message : "No message content");
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position < 0 || position >= chatMessages.size()) {
            Log.e(TAG, "Invalid position for view type: " + position);
            return 1; // Default to AI message type as a fallback
        }
        return chatMessages.get(position).isUser() ? 0 : 1; // 0 = user, 1 = AI
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.message_text);
        }
    }

    // Update chat messages and refresh RecyclerView
    public void updateChat(List<ChatMessage> newMessages) {
        chatMessages.clear();
        chatMessages.addAll(newMessages);
        notifyDataSetChanged();
    }
}
