package com.example.calodiary.post.ai;



import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.calodiary.chat.ChatGPTClient;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class AutoCreatePostWorker extends Worker {
    private FirebaseFirestore db;

    public AutoCreatePostWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public Result doWork() {
        // Dùng CountDownLatch để chờ API phản hồi trước khi tiếp tục
        CountDownLatch latch = new CountDownLatch(1);
        final String[] generatedContent = {""};
        final String[] generatedImage = {""};

        ChatGPTClient.sendMessage("Hãy tạo một bài post về sức khỏe hoặc dinh dưỡng.", new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("AutoCreatePostWorker", "ChatGPT API call failed", e);
                latch.countDown();  // Giảm đếm dù thất bại
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    generatedContent[0] = response.body().string();
                } else {
                    Log.e("AutoCreatePostWorker", "Failed to get valid response from ChatGPT");
                }
                latch.countDown();  // Giảm đếm để tiếp tục tiến trình
            }
        });

        try {
            latch.await(); // Chờ API phản hồi trước khi tiếp tục
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Result.failure();
        }

        CountDownLatch imageLatch = new CountDownLatch(1);

        ChatGPTClient.generateImage("Hình ảnh minh họa về: " + generatedContent[0], new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("AutoCreatePostWorker", "DALL·E API call failed", e);
                imageLatch.countDown();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    JsonObject jsonResponse = JsonParser.parseString(responseData).getAsJsonObject();
                    JsonArray images = jsonResponse.getAsJsonArray("data");
                    if (images != null && images.size() > 0) {
                        generatedImage[0] = images.get(0).getAsJsonObject().get("url").getAsString();
                    }
                } else {
                    Log.e("AutoCreatePostWorker", "Failed to get image from DALL·E");
                }
                imageLatch.countDown();
            }
        });

        try {
            imageLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Result.failure();
        }

        // Create a new PostAI object
        PostAI newPost = new PostAI(
                generatedContent[0].substring(0, 100),
                generatedContent[0].isEmpty() ? "Bài viết tự động nhưng không có nội dung từ AI." : generatedContent[0],
                "ai", // Replace with a valid authorId
                "pending",
                generatedImage[0], // Optional: Add Base64 image string if needed
                true,
                Timestamp.now(),
                Timestamp.now()
        );

        // Save to Firestore
        try {
            db.collection("posts").add(newPost);
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.retry(); // Retry if it fails
        }
    }


}
