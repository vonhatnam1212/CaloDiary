package com.example.calodiary.chat;

import okhttp3.*;

import com.example.calodiary.BuildConfig;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

public class ChatGPTClient {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String IMAGE_API_URL = "https://api.openai.com/v1/images/generations";
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
        json.addProperty("max_tokens", 1000);
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

    public static void generateImage(String prompt, Callback callback) {
        String API_KEY = BuildConfig.OPENAI_API_KEY;

        // Ensure API key is available
        if (API_KEY == null || API_KEY.isEmpty()) {
            throw new RuntimeException("Missing API key: OPENAI_API_KEY");
        }

        // Increase timeout to handle slow responses
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)  // Connection timeout
                .readTimeout(120, TimeUnit.SECONDS)    // Read timeout
                .writeTimeout(120, TimeUnit.SECONDS)   // Write timeout
                .build();

        // Construct JSON request body
        JsonObject json = new JsonObject();
        json.addProperty("model", "dall-e-2");
        json.addProperty("prompt", prompt);
        json.addProperty("n", 1);
        json.addProperty("size", "1024x1024");

        // Create request
        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(IMAGE_API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        // Execute request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.err.println("DALL·E API call failed: " + e.getMessage());
                callback.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseData = response.body().string();
                        JsonObject jsonResponse = JsonParser.parseString(responseData).getAsJsonObject();
                        JsonArray images = jsonResponse.getAsJsonArray("data");

                        if (images != null && images.size() > 0) {
                            String imageUrl = images.get(0).getAsJsonObject().get("url").getAsString();
                            System.out.println("Generated Image URL: " + imageUrl);

                            // Pass the successful response
                            callback.onResponse(call, new Response.Builder()
                                    .request(call.request())
                                    .protocol(Protocol.HTTP_1_1)
                                    .code(200)
                                    .message("OK")
                                    .body(ResponseBody.create(imageUrl, JSON))
                                    .build());
                        } else {
                            System.err.println("DALL·E API returned no images.");
                            callback.onFailure(call, new IOException("No images generated."));
                        }
                    } catch (Exception e) {
                        callback.onFailure(call, new IOException("Failed to parse DALL·E response"));
                    }
                } else {
                    System.err.println("DALL·E API Error: " + response.code());
                    callback.onFailure(call, new IOException("Error: " + response.code()));
                }
            }
        });
    }
}
