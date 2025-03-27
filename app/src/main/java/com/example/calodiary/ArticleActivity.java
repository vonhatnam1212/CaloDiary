package com.example.calodiary;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
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
    private ImageView btnLike, btnComment, btnSendComment, articleAvatar, articleImage;
    private TextView txtLikeCount, txtCommentCount, articleUsername, articleContent, commentList;
    private EditText editComment;
    private View commentSection;
    private boolean isLiked = false; // Trạng thái like
    private int likeCount = 0; // Số lượt thích
    private ArrayList<String> comments = new ArrayList<>(); // Danh sách bình luận

    public void addSamplePosts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
    }

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

                            // Lấy số lượng comment từ collection "comments"
                            db.collection("comments")
                                    .whereEqualTo("postId", post.getId())
                                    .get()
                                    .addOnCompleteListener(commentTask -> {
                                        if (commentTask.isSuccessful()) {
                                            int commentCount = commentTask.getResult().size();
                                            post.setCommentCount(commentCount);
                                            postAdapter.notifyDataSetChanged(); // Cập nhật UI
                                        }
                                    });

                            postHomeList.add(post);
                        }
                        postAdapter.notifyDataSetChanged();
                    }
                });
    }

}
