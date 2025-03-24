package com.example.calodiary.post.ai;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AutoCreatePostWorker extends Worker {
    private FirebaseFirestore db;

    public AutoCreatePostWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public Result doWork() {
        // Create a new PostAI object
        PostAI newPost = new PostAI(
                "Auto-Generated Post " + System.currentTimeMillis(),
                "This is an automatically created post from WorkManager.",
                "system", // Replace with a valid authorId
                "pending",
                "", // Optional: Add Base64 image string if needed
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
