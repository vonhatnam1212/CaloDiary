package com.example.calodiary.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calodiary.ArticleActivity;
import com.example.calodiary.HomeActivity;
import com.example.calodiary.Profile;
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
    private LinearLayout tabHome, tabBlog, tabChat, tabProfile;
    private ImageView iconHome, iconBlog, iconChat, iconProfile;

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
        setupCustomBottomNavigation();
    }

    private void setSelectedTab(LinearLayout tab, ImageView icon) {
        // Reset all tabs to unselected state
        tabHome.setSelected(false);
        tabBlog.setSelected(false);
        tabChat.setSelected(false);
        tabProfile.setSelected(false);

        iconHome.setColorFilter(getResources().getColor(android.R.color.darker_gray));
        iconBlog.setColorFilter(getResources().getColor(android.R.color.darker_gray));
        iconChat.setColorFilter(getResources().getColor(android.R.color.darker_gray));
        iconProfile.setColorFilter(getResources().getColor(android.R.color.darker_gray));

        // Set selected tab
        tab.setSelected(true);
        icon.setColorFilter(getResources().getColor(R.color.primary));
    }

    private boolean isCurrentActivity(Class<?> activityClass) {
        return getClass().getName().equals(activityClass.getName());
    }

    private void navigateToActivity(Class<?> destinationActivity) {
        try {
            Intent intent = new Intent(this, destinationActivity);
            startActivity(intent);
            finish(); // Finish current activity to avoid stacking
        } catch (Exception e) {
            Toast.makeText(this, "Không thể mở màn hình này", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupCustomBottomNavigation() {
        // Initialize bottom navigation
        View bottomNav = findViewById(R.id.bottomNavigation);
        tabHome = bottomNav.findViewById(R.id.tab_home);
        tabBlog = bottomNav.findViewById(R.id.tab_blog);
        tabChat = bottomNav.findViewById(R.id.tab_chat);
        tabProfile = bottomNav.findViewById(R.id.tab_profile);

        iconHome = bottomNav.findViewById(R.id.icon_home);
        iconBlog = bottomNav.findViewById(R.id.icon_blog);
        iconChat = bottomNav.findViewById(R.id.icon_chat);
        iconProfile = bottomNav.findViewById(R.id.icon_profile);

        // Set chat tab as selected
        setSelectedTab(tabChat, iconChat);

        // Set click listeners for each tab
        tabHome.setOnClickListener(v -> {
            setSelectedTab(tabHome, iconHome);
            navigateToActivity(HomeActivity.class);
        });

        tabBlog.setOnClickListener(v -> {
            setSelectedTab(tabBlog, iconBlog);
            navigateToActivity(ArticleActivity.class);
        });

        tabChat.setOnClickListener(v -> {
            if (!isCurrentActivity(ChatActivity.class)) {
                setSelectedTab(tabChat, iconChat);
                // Already in ChatActivity
            }
        });

        tabProfile.setOnClickListener(v -> {
            setSelectedTab(tabProfile, iconProfile);
            navigateToActivity(Profile.class);
        });
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
