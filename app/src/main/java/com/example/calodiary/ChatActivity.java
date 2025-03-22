package com.example.calodiary;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import java.io.IOException;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private EditText inputMessage;
    private Button sendButton;

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

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = inputMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    chatMessages.add(new ChatMessage(message, true));
                    chatAdapter.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(chatMessages.size() - 1);

                    // Call API to get AI response
                    callChatGPT(message);

                    // Clear input field
                    inputMessage.setText("");
                }
            }
        });
    }

    private void callChatGPT(String message) {
        ChatGPTClient.sendMessage(message, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    chatMessages.add(new ChatMessage("Error: " + e.getMessage(), false));
                    chatAdapter.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();

                    runOnUiThread(() -> {
                        chatMessages.add(new ChatMessage(responseData, false));  // AI response
                        chatAdapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                    });
                } else {
                    runOnUiThread(() -> {
                        chatMessages.add(new ChatMessage("Error: " + response.code(), false));
                        chatAdapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                    });
                }
            }
        });
    }
}
