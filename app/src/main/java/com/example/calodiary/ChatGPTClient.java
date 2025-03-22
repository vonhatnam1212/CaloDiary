package com.example.calodiary;

import okhttp3.*;
import com.google.gson.JsonObject;
import java.io.IOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import android.util.Log;
public class ChatGPTClient {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static void sendMessage(String userMessage, Callback callback) {
        String API_KEY = BuildConfig.OPENAI_API_KEY;
        if (API_KEY == null || API_KEY.isEmpty()) {
            throw new RuntimeException("Missing environment variable: OPENAI_API_KEY");
        }
        OkHttpClient client = new OkHttpClient();

        // Construct the JSON payload
        JsonObject json = new JsonObject();
        json.addProperty("model", "gpt-4o-mini");
        json.addProperty("max_tokens", 100);
        json.addProperty("temperature", 0.7);

        // Create the message object
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", userMessage);

        // Add the message to the messages array
        json.add("messages", new com.google.gson.JsonArray());
        json.getAsJsonArray("messages").add(message);

        // Build the request body
        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));
        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();

        // Execute the request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(call, e);  // Handle errors
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // Parse the JSON response
                        String responseData = response.body().string();
                        JsonObject jsonResponse = JsonParser.parseString(responseData).getAsJsonObject();

                        // Extract the assistant's reply
                        JsonArray choices = jsonResponse.getAsJsonArray("choices");
                        String botMessage = choices.get(0).getAsJsonObject().getAsJsonObject("message").get("content").getAsString();

                        // Pass the botMessage to callback
                        callback.onResponse(call, new Response.Builder()
                                .request(call.request())
                                .protocol(Protocol.HTTP_1_1)
                                .code(200)
                                .message("OK")
                                .body(ResponseBody.create(botMessage, MediaType.get("application/json; charset=utf-8")))
                                .build()
                        );

                    } catch (Exception e) {
                        callback.onFailure(call, new IOException("Failed to parse response"));
                    }
                } else {
                    callback.onFailure(call, new IOException("Error: " + response.code()));
                }
            }
        });
    }
}
