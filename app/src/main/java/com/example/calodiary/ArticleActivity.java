package com.example.calodiary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
    
    // Custom bottom navigation views
    private LinearLayout tabHome, tabBlog, tabChat, tabProfile;
    private ImageView iconHome, iconBlog, iconChat, iconProfile;

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

        setupCustomBottomNavigation();
    }

    private void setupCustomBottomNavigation() {
        // Initialize bottom navigation
        View bottomNav = findViewById(R.id.bottomNavigation);
        tabHome = bottomNav.findViewById(R.id.tab_home);
        tabBlog = bottomNav.findViewById(R.id.tab_blog);
        tabChat = bottomNav.findViewById(R.id.tab_chat);
        tabProfile = bottomNav.findViewById(R.id.tab_profile);
        
        iconHome = bottomNav.findViewById(R.id.icon_home);
        iconBlog = bottomNav.findViewById(R.id.icon_blog);
        iconChat = bottomNav.findViewById(R.id.icon_chat);
        iconProfile = bottomNav.findViewById(R.id.icon_profile);
        
        // Set blog tab as selected
        setSelectedTab(tabBlog, iconBlog);
        
        // Set click listeners for each tab
        tabHome.setOnClickListener(v -> {
            setSelectedTab(tabHome, iconHome);
            navigateToActivity(HomeActivity.class);
        });
        
        tabBlog.setOnClickListener(v -> {
            if (!isCurrentActivity(ArticleActivity.class)) {
                setSelectedTab(tabBlog, iconBlog);
                // Already in ArticleActivity
            }
        });
        
        tabChat.setOnClickListener(v -> {
            setSelectedTab(tabChat, iconChat);
            // Navigate to Chat - Assume you have a ChatActivity class
            try {
                navigateToActivity(Class.forName("com.example.calodiary.ChatActivity"));
            } catch (ClassNotFoundException e) {
                Toast.makeText(this, "Chat feature coming soon", Toast.LENGTH_SHORT).show();
            }
        });
        
        tabProfile.setOnClickListener(v -> {
            setSelectedTab(tabProfile, iconProfile);
            navigateToActivity(Profile.class);
        });
    }
    
    private void setSelectedTab(LinearLayout tab, ImageView icon) {
        // Reset all tabs to unselected state
        tabHome.setSelected(false);
        tabBlog.setSelected(false);
        tabChat.setSelected(false);
        tabProfile.setSelected(false);
        
        iconHome.setColorFilter(getResources().getColor(android.R.color.darker_gray));
        iconBlog.setColorFilter(getResources().getColor(android.R.color.darker_gray));
        iconChat.setColorFilter(getResources().getColor(android.R.color.darker_gray));
        iconProfile.setColorFilter(getResources().getColor(android.R.color.darker_gray));
        
        // Set selected tab
        tab.setSelected(true);
        icon.setColorFilter(getResources().getColor(R.color.primary));
    }
    
    private boolean isCurrentActivity(Class<?> activityClass) {
        return getClass().getName().equals(activityClass.getName());
    }
    
    private void navigateToActivity(Class<?> destinationActivity) {
        try {
            Intent intent = new Intent(this, destinationActivity);
            startActivity(intent);
            finish(); // Finish current activity to avoid stacking
        } catch (Exception e) {
            Toast.makeText(this, "Không thể mở màn hình này", Toast.LENGTH_SHORT).show();
        }
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
