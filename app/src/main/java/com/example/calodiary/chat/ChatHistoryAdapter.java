package com.example.calodiary.chat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.example.calodiary.R;

public class ChatHistoryAdapter extends RecyclerView.Adapter<ChatHistoryAdapter.ChatHistoryViewHolder> {
    private List<ChatSession> chatSessions;
    private Context context;
    private OnDeleteClickListener deleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(String sessionId);
    }

    public ChatHistoryAdapter(Context context, List<ChatSession> chatSessions) {
        this.context = context;
        this.chatSessions = chatSessions;
    }

    public ChatHistoryAdapter(Context context, List<ChatSession> chatSessions, OnDeleteClickListener listener) {
        this.context = context;
        this.chatSessions = chatSessions;
        this.deleteClickListener = listener;
    }

    @NonNull
    @Override
    public ChatHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_history, parent, false);
        return new ChatHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatHistoryViewHolder holder, int position) {
        ChatSession chatSession = chatSessions.get(position);

        // Ensure timestamp is not null
        Date timestamp = chatSession.getTimestamp();
        String formattedDate = (timestamp != null)
                ? new SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault()).format(timestamp)
                : "No Date";

        // Get the last message from the chat session
        String lastMessageText = "No messages yet";
        if (chatSession.getMessages() != null && !chatSession.getMessages().isEmpty()) {
            ChatMessage lastMessage = chatSession.getMessages().get(chatSession.getMessages().size() - 1);
            lastMessageText = lastMessage.getMessage();
        }

        // Bind data to views
        holder.lastMessage.setText(lastMessageText);
        holder.timestamp.setText(formattedDate);

        // Handle click event
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("sessionId", chatSession.getSessionId());
            context.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(chatSession.getSessionId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatSessions.size();
    }

    public static class ChatHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView lastMessage, timestamp;
        Button btnDelete;

        public ChatHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            timestamp = itemView.findViewById(R.id.timestamp);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
