package com.example.calodiary;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calodiary.Model.Posts;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class DisplayCreatedPostActivity extends AppCompatActivity {
    private FloatingActionButton fb;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private FirebaseFirestore db;
    private List<Posts> postsList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_display_created_post);

        fb = findViewById(R.id.fb);
        recyclerView = findViewById(R.id.recycleView);

        db = FirebaseFirestore.getInstance();
        postsList = new ArrayList<>();

        loadPostsFromFirestore();

        postAdapter = new PostAdapter(DisplayCreatedPostActivity.this, postsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(DisplayCreatedPostActivity.this));
        recyclerView.setAdapter(postAdapter);

        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DisplayCreatedPostActivity.this, UploadPostActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadPostsFromFirestore() {
        db.collection("posts")
                .whereEqualTo("status", "pending")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            postsList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Posts post = document.toObject(Posts.class);
                                postsList.add(post);
                            }
                            postAdapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}