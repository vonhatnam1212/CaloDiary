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
        // Sự kiện click vào ảnh để mở ImageViewActivity
        holder.imgPost.setOnClickListener(v -> {
            Intent intent = new Intent(context, ImageViewActivity.class);
            intent.putExtra("image_base64", imgPath); // Truyền Base64 qua Intent
            context.startActivity(intent);
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
        ImageView imgPost;
        TextView txtCaption, txtLikeCount;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPost = itemView.findViewById(R.id.imgPost);
            txtCaption = itemView.findViewById(R.id.txtCaption);
        }
    }
}