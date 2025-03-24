package com.example.calodiary.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.example.calodiary.R;

public class ChatHistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatHistoryAdapter chatHistoryAdapter;
    private List<ChatSession> chatSessions;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private Button btnNewChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_history);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        btnNewChat = findViewById(R.id.btnNewChat);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatSessions = new ArrayList<>();
        chatHistoryAdapter = new ChatHistoryAdapter(this, chatSessions, this::deleteChatSession);
        recyclerView.setAdapter(chatHistoryAdapter);

        db = FirebaseFirestore.getInstance();
        loadChatSessions();

        btnNewChat.setOnClickListener(v -> createNewChatSession());
    }

    private void loadChatSessions() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("chat_sessions")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error loading chats", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    chatSessions.clear();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            ChatSession chatSession = doc.toObject(ChatSession.class);
                            if (chatSession != null) {
                                chatSessions.add(chatSession);
                            }
                        }
                    }
                    chatHistoryAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void createNewChatSession() {
        String newSessionId = UUID.randomUUID().toString();
        ChatMessage newChatMessage= new ChatMessage("Hello", false);
        ChatSession newChatSession = new ChatSession(newSessionId, "New Chat", new ArrayList<>());
        newChatSession.getMessages().add(newChatMessage);

        db.collection("chat_sessions")
                .document(newSessionId)
                .set(newChatSession)
                .addOnSuccessListener(aVoid -> openChatSession(newSessionId))
                .addOnFailureListener(e ->
                        Toast.makeText(ChatHistoryActivity.this, "Failed to create chat", Toast.LENGTH_SHORT).show()
                );
    }

    private void deleteChatSession(String sessionId) {
        db.collection("chat_sessions")
                .document(sessionId)
                .delete()
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(ChatHistoryActivity.this, "Chat deleted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(ChatHistoryActivity.this, "Failed to delete chat", Toast.LENGTH_SHORT).show()
                );
    }

    private void openChatSession(String sessionId) {
        Intent intent = new Intent(ChatHistoryActivity.this, ChatActivity.class);
        intent.putExtra("sessionId", sessionId);
        startActivity(intent);
    }
}
