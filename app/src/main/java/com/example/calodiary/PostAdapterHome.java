package com.example.calodiary;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class PostAdapterHome extends RecyclerView.Adapter<PostAdapterHome.PostViewHolder> {
    private List<PostHome> postList;
    private Context context;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public PostAdapterHome(Context context, List<PostHome> postList) {
        this.context = context;
        this.postList = postList;
    }

    public PostAdapterHome(List<PostHome> postList) {
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        PostHome post = postList.get(position);
        String imgPath = post.getImg();
        holder.txtCaption.setText(post.getTitle());
        byte[] bytes = Base64.decode(imgPath, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        Glide.with(context)
                .load(bitmap)
                .into(holder.imgPost);
        holder.txtLikeCount.setText(post.getLikeCount() + " likes");
        holder.btnLike.setImageResource(post.isLiked() ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        // Sự kiện click vào ảnh để mở ImageViewActivity
        holder.imgPost.setOnClickListener(v -> {
            Intent intent = new Intent(context, ImageViewActivity.class);
            intent.putExtra("image_base64", imgPath); // Truyền Base64 qua Intent
            context.startActivity(intent);
        });


        // Hiển thị 2 bình luận mới nhất (nếu có)
        List<Comment> comments = post.getComments();
        if (comments != null && !comments.isEmpty()) {
            holder.txtComment1.setText(comments.get(0).getText());
            holder.txtComment1.setVisibility(View.VISIBLE);

            if (comments.size() > 1) {
                holder.txtComment2.setText(comments.get(1).getText());
                holder.txtComment2.setVisibility(View.VISIBLE);
            } else {
                holder.txtComment2.setVisibility(View.GONE);
            }
        } else {
            holder.txtComment1.setVisibility(View.GONE);
            holder.txtComment2.setVisibility(View.GONE);
        }
        // Sự kiện bấm vào nút like
        holder.btnLike.setOnClickListener(v -> {
            boolean isLiked = post.isLiked(); // Kiểm tra trạng thái hiện tại (cần thêm biến trong PostHome)
            if (isLiked) {
                post.setLikeCount(post.getLikeCount() - 1);
                holder.btnLike.setImageResource(R.drawable.ic_heart_outline); // Icon chưa like
            } else {
                post.setLikeCount(post.getLikeCount() + 1);
                holder.btnLike.setImageResource(R.drawable.ic_heart_filled); // Icon đã like
            }
            post.setLiked(!isLiked); // Đảo trạng thái like
            holder.txtLikeCount.setText(String.valueOf(post.getLikeCount()));
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference postRef = db.collection("posts").document(post.getId());

            // Cập nhật dữ liệu bài viết
            postRef.update("likeCount", post.getLikeCount(), "liked", post.isLiked())
                    .addOnSuccessListener(aVoid -> {
                        // Thành công khi cập nhật
                        Log.d("Firebase", "Cập nhật like thành công!");
                    })
                    .addOnFailureListener(e -> {
                        // Lỗi khi cập nhật
                        Log.e("Firebase", "Cập nhật like thất bại: " + e.getMessage());
                    });
        });

        // Sự kiện bấm vào ảnh bài viết -> Chuyển sang màn hình chi tiết
        holder.imgPost.setOnClickListener(v -> {
            Log.d("PostAdapterHome", "Clicked Post ID: " + post.getId()); // Kiểm tra ID trước khi gửi
            Intent intent = new Intent(context, ImageViewActivity.class);
            intent.putExtra("post_id", post.getId()); // Truyền ID bài viết
            context.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPost, btnLike, btnComment;
        TextView txtCaption, txtLikeCount, txtCommentCount, txtComment1, txtComment2;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPost = itemView.findViewById(R.id.imgPost);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            txtCaption = itemView.findViewById(R.id.txtCaption);
            txtLikeCount = itemView.findViewById(R.id.txtLikeCount);
            txtCommentCount = itemView.findViewById(R.id.txtCommentCount);
            txtComment1 = itemView.findViewById(R.id.txtComment1);
            txtComment2 = itemView.findViewById(R.id.txtComment2);
        }
    }
}