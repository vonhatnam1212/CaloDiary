package com.example.calodiary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ArticleActivity extends AppCompatActivity {
    private List<PostHome> postHomeList;
    private RecyclerView recyclerView;
    private PostAdapterHome postAdapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        // Ánh xạ view
        recyclerView = findViewById(R.id.recycleView);
        //bat dau load
        db = FirebaseFirestore.getInstance();
        postHomeList = new ArrayList<>();
        loadPostsFromFirestore();

        postAdapter = new PostAdapterHome(ArticleActivity.this, postHomeList);
        recyclerView.setLayoutManager(new LinearLayoutManager(ArticleActivity.this));
        recyclerView.setAdapter(postAdapter);


        // Xử lý Like bài viết
    }

    private void loadPostsFromFirestore() {
        db.collection("posts")
                .whereEqualTo("status", "approved")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        postHomeList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            PostHome post = document.toObject(PostHome.class);
                            postHomeList.add(post);
                        }
                        postAdapter.notifyDataSetChanged();
                    }
                });
    }
    //phần nào xử lý button điều hướng article dùng dòng này:
//    CardView cvArticle = findViewById(R.id.cvArticle);
//        cvArticle.setOnClickListener(v -> {
//        Intent intent = new Intent(HomeActivity.this, ArticleActivity.class);
//        startActivity(intent);
//    });

}
