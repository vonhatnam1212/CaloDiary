package com.example.calodiary.post.ai;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.calodiary.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ListPostAIActivity extends AppCompatActivity {
    private FloatingActionButton fb;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private ListPostAIAdapter listPostAIAdapter;
    private FirebaseFirestore db;
    private List<PostAI> postsList;
    private List<PostAI> searchList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_display_created_post_ai);

//        fb = findViewById(R.id.fb);
        recyclerView = findViewById(R.id.recycleView);
        searchView = findViewById(R.id.search);
        db = FirebaseFirestore.getInstance();
        postsList = new ArrayList<>();
        searchList = new ArrayList<>();

        loadPostsFromFirestore();

        listPostAIAdapter = new ListPostAIAdapter(ListPostAIActivity.this, searchList);
        recyclerView.setLayoutManager(new LinearLayoutManager(ListPostAIActivity.this));
        recyclerView.setAdapter(listPostAIAdapter);

//        fb.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(ListPostAIActivity.this, UploadPostAIActivity.class);
//                startActivity(intent);
//            }
//        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                return true;
            }
        });


        schedulePostCreation();
    }

    private void schedulePostCreation() {
        // Define the periodic work request (e.g., every 24 hours)
        PeriodicWorkRequest postCreationRequest =
                new PeriodicWorkRequest.Builder(AutoCreatePostWorker.class, 1, TimeUnit.HOURS)
                        .build();

        // Enqueue the work with a unique name to avoid duplicates
        WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork(
                        "postCreationWork",
                        ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, // Keep existing work if already scheduled
                        postCreationRequest
                );
    }


    private void loadPostsFromFirestore() {
        db.collection("posts")
                .whereEqualTo("status", "pending")
                .whereEqualTo("ai", true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            postsList.clear();
                            searchList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                PostAI post = document.toObject(PostAI.class);
                                postsList.add(post);
                                searchList.add(post);
                            }
                            listPostAIAdapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void search(String query) {
        searchList.clear();
        if (query.isEmpty()) {
            searchList.addAll(postsList);
        } else {
            String queryLower = query.toLowerCase();
            for (PostAI post : postsList) {
                if (post.getTitle().toLowerCase().contains(queryLower)) {
                    searchList.add(post);
                }
            }
        }
        listPostAIAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPostsFromFirestore();
    }
}