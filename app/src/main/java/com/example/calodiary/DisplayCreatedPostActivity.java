package com.example.calodiary;

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
    private SearchView searchView;
    private PostAdapter postAdapter;
    private FirebaseFirestore db;
    private List<Posts> postsList;
    private List<Posts> searchList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_display_created_post);

        fb = findViewById(R.id.fb);
        recyclerView = findViewById(R.id.recycleView);
        searchView = findViewById(R.id.search);
        db = FirebaseFirestore.getInstance();
        postsList = new ArrayList<>();
        searchList = new ArrayList<>();

        loadPostsFromFirestore();

        postAdapter = new PostAdapter(DisplayCreatedPostActivity.this, searchList);
        recyclerView.setLayoutManager(new LinearLayoutManager(DisplayCreatedPostActivity.this));
        recyclerView.setAdapter(postAdapter);

        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DisplayCreatedPostActivity.this, UploadPostActivity.class);
                startActivity(intent);
            }
        });

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
    }


    private void loadPostsFromFirestore() {
        db.collection("posts")
                .whereEqualTo("status", "pending")
                .whereEqualTo("ai", false)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            postsList.clear();
                            searchList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Posts post = document.toObject(Posts.class);
                                postsList.add(post);
                                searchList.add(post);
                            }
                            postAdapter.notifyDataSetChanged();
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
            for (Posts post : postsList) {
                if (post.getTitle().toLowerCase().contains(queryLower)) {
                    searchList.add(post);
                }
            }
        }
        postAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPostsFromFirestore();
    }
}