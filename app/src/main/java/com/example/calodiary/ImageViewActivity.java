package com.example.calodiary;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImageViewActivity extends AppCompatActivity {
    private ImageView imgPost, btnLike, btnComment;
    private TextView txtTitle, txtContent, txtLikeCount, txtCommentCount,btnSendComment;
    private EditText edtComment;
    private RecyclerView recyclerViewComments;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private PostHome post;
    private String postId, documentId;
    private boolean isLiked;
    private int likeCount;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);

        imgPost = findViewById(R.id.imgPost);
        btnLike = findViewById(R.id.btnLike);
        btnComment = findViewById(R.id.btnComment);
        txtTitle = findViewById(R.id.txtTitle);
        txtContent = findViewById(R.id.txtContent);
        txtLikeCount = findViewById(R.id.txtLikeCount);
        txtCommentCount = findViewById(R.id.txtCommentCount);
        edtComment = findViewById(R.id.edtComment);
        recyclerViewComments = findViewById(R.id.recyclerComments);
        btnSendComment = findViewById(R.id.btnSendComment);


        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(this, commentList);
        recyclerViewComments.setAdapter(commentAdapter);

        postId = getIntent().getStringExtra("post_id");
        Log.d("ImageViewActivity", "Received Post ID: " + postId);

        if (postId != null) {
            loadPostData();
        } else {
            Log.e("ImageViewActivity", "No Post ID received");
        }

        btnLike.setOnClickListener(v -> toggleLike());
        btnComment.setOnClickListener(v -> toggleCommentBox());
        btnSendComment.setOnClickListener(v -> {
            String commentText = edtComment.getText().toString().trim();
            if (!commentText.isEmpty()) {
                addCommentToFirestore(commentText);
                edtComment.setText(""); // Xóa ô nhập sau khi gửi
            }
        });
    }

    private void loadPostData() {
        db.collection("posts").whereEqualTo("id", postId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Log.e("ImageViewActivity", "Post not found in Firestore");
                        return;
                    }

                    DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                    documentId = documentSnapshot.getId();
                    post = documentSnapshot.toObject(PostHome.class);

                    if (post != null) {
                        txtTitle.setText(post.getTitle());
                        txtContent.setText(post.getContent());
                        displayPostImage(post.getImg());

                        isLiked = post.isLiked();
                        likeCount = post.getLikeCount();
                        updateLikeUI();

                        txtLikeCount.setText(likeCount + " likes");
                        txtCommentCount.setText(post.getCommentCount() + " comments");

                        loadComments();
                    }
                })
                .addOnFailureListener(e -> Log.e("ImageViewActivity", "Firestore error: " + e.getMessage()));
    }

    private void displayPostImage(String imgData) {
        if (imgData == null || imgData.isEmpty()) return;

        if (imgData.startsWith("http")) {
            Glide.with(this).load(imgData).into(imgPost);
        } else {
            try {
                byte[] bytes = Base64.decode(imgData, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imgPost.setImageBitmap(bitmap);
            } catch (Exception e) {
                Log.e("ImageViewActivity", "Error decoding Base64 image: " + e.getMessage());
            }
        }
    }

    private void addCommentToFirestore(String commentText) {
        Map<String, Object> comment = new HashMap<>();
        comment.put("text", commentText);
        comment.put("timestamp", System.currentTimeMillis());

        db.collection("posts").document(documentId).collection("comments")
                .add(comment)
                .addOnSuccessListener(documentReference -> {
                    Log.d("ImageViewActivity", "Bình luận đã được thêm");
                    loadComments(); // Cập nhật danh sách bình luận
                })
                .addOnFailureListener(e -> Log.e("ImageViewActivity", "Lỗi khi thêm bình luận", e));
    }


    public void loadComments() {
        db.collection("posts").document(documentId).collection("comments")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    commentList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Comment comment = doc.toObject(Comment.class);
                        commentList.add(comment);
                    }
                    commentAdapter.notifyDataSetChanged();

                    // Cập nhật số lượng bình luận
                    txtCommentCount.setText(commentList.size() + " comments");

                    Log.d("ImageViewActivity", "Loaded " + commentList.size() + " comments");
                })
                .addOnFailureListener(e -> Log.e("ImageViewActivity", "Failed to load comments: " + e.getMessage()));
    }

    private void toggleLike() {
        isLiked = !isLiked;
        likeCount += isLiked ? 1 : -1;
        updateLikeUI();

        Map<String, Object> updates = new HashMap<>();
        updates.put("liked", isLiked);
        updates.put("likeCount", likeCount);

        db.collection("posts").document(documentId)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    txtLikeCount.setText(likeCount + " likes");
                    Log.d("ImageViewActivity", "Like updated successfully");
                })
                .addOnFailureListener(e -> Log.e("ImageViewActivity", "Failed to update like: " + e.getMessage()));
    }

    private void toggleCommentBox() {
        if (edtComment.getVisibility() == View.GONE) {
            edtComment.setVisibility(View.VISIBLE);
        } else {
            edtComment.setVisibility(View.GONE);
        }
    }

    private void updateLikeUI() {
        btnLike.setImageResource(isLiked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("post_id", postId);
        resultIntent.putExtra("liked", isLiked);
        resultIntent.putExtra("like_count", likeCount);
        setResult(RESULT_OK, resultIntent);
        super.onBackPressed();
    }

}
