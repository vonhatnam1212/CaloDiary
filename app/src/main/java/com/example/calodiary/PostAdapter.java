package com.example.calodiary;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.calodiary.Model.Posts;


import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private Context context;
    private List<Posts> postsList;

    public PostAdapter(Context context, List<Posts> postsList) {
        this.context = context;
        this.postsList = postsList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_recycle_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Posts post = postsList.get(position);
        holder.recTitle.setText(post.getTitle());
        String imgPath = post.getImg();

        byte[] bytes= Base64.decode(imgPath,Base64.DEFAULT);

        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);

        Glide.with(context)
                .load(bitmap)
                .error(R.drawable.upload)
                .into(holder.recImage);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailPostActivity.class);
                intent.putExtra("postID", post.getId());
                context.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return postsList != null ? postsList.size() : 0;
    }


    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView recImage;
        TextView recTitle;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            recImage = itemView.findViewById(R.id.recImage);
            recTitle = itemView.findViewById(R.id.recTitle);
        }
    }
}
