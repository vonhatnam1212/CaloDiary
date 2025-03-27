package com.example.calodiary.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calodiary.ArticleActivity;
import com.example.calodiary.HomeActivity;
import com.example.calodiary.Profile;
import com.example.calodiary.R;
import com.google.firebase.firestore.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private EditText inputMessage;
    private Button sendButton;
    private FirebaseFirestore db;
    
    // Custom bottom navigation views
    private LinearLayout tabHome, tabBlog, tabChat, tabProfile;
    private ImageView iconHome, iconBlog, iconChat, iconProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.recyclerView);
        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        db = FirebaseFirestore.getInstance();
        final String sessionId = getIntent().getStringExtra("sessionId");
        if (sessionId == null) {
            Toast.makeText(this, "No session ID provided", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if no sessionId
            return;
        }

        loadChatHistory(sessionId);
        setupCustomBottomNavigation();

        sendButton.setOnClickListener(v -> sendMessage(sessionId));
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

    private void sendMessage(String sessionId) {
        // Safely get the message text
        String message;
        try {
            message = inputMessage.getText().toString().trim();
        } catch (NullPointerException e) {
            Toast.makeText(this, "Error: Message input is not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the message is valid
        if (message.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        // Proceed with sending the message
        ChatMessage userMessage = new ChatMessage(message, true); // true = user message
        chatMessages.add(userMessage);
        chatAdapter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(chatMessages.size() - 1);

        saveMessageToFirestore(sessionId, userMessage);
        inputMessage.setText(""); // Clear input
        callChatGPT(sessionId, message);
    }

    private void loadChatHistory(String sessionId) {
        db.collection("chat_sessions").document(sessionId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        ChatSession chatSession = documentSnapshot.toObject(ChatSession.class);
                        if (chatSession != null && chatSession.getMessages() != null) {
                            chatMessages.addAll(chatSession.getMessages());
                            chatAdapter.notifyDataSetChanged();
                            recyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load chat", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveNewChatSession(String sessionId) {
        db.collection("chat_sessions")
                .document(sessionId)
                .set(new ChatSession(sessionId, "New chat", chatMessages));
    }

    private void saveMessageToFirestore(String sessionId,ChatMessage message) {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("message", message.getMessage());
        messageMap.put("isUser", message.isUser());
        messageMap.put("timestamp", new Date()); // Use Firestore Timestamp
        db.collection("chat_sessions")
                .document(sessionId)
                .update("messages", FieldValue.arrayUnion(messageMap));
    }

    private void callChatGPT(String sessionId,String message) {
        ChatGPTClient.sendMessage(message, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    ChatMessage errorMessage = new ChatMessage("Error: " + e.getMessage(), false);
                    chatMessages.add(errorMessage);
                    chatAdapter.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                    saveMessageToFirestore(sessionId,errorMessage);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    runOnUiThread(() -> {
                        ChatMessage aiResponse = new ChatMessage(responseData, false);
                        chatMessages.add(aiResponse);
                        chatAdapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                        saveMessageToFirestore(sessionId,aiResponse);
                    });
                } else {
                    runOnUiThread(() -> {
                        ChatMessage errorResponse = new ChatMessage("Error: " + response.code(), false);
                        chatMessages.add(errorResponse);
                        chatAdapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                        saveMessageToFirestore(sessionId,errorResponse);
                    });
                }
            }
        });
    }
}
