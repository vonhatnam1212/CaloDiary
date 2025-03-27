package com.example.calodiary.post.ai;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.calodiary.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Locale;
public class DetailPostAIActivity extends AppCompatActivity{
    private TextView detailTitle;
    private ImageView detailImage;
    private TextView createdAt;
    private TextView updatedAt;
    private TextView content;
    private FirebaseFirestore db;
    private FloatingActionButton acceptButton, rejectButton, updateButton;

    private String documentId;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail_post_ai);

        detailTitle = findViewById(R.id.detailTitle);
        detailImage = findViewById(R.id.detailImage);
        createdAt = findViewById(R.id.createdAt);
        updatedAt = findViewById(R.id.updatedAt);
        content = findViewById(R.id.content);
        rejectButton = findViewById(R.id.rejectButton);
        acceptButton = findViewById(R.id.acceptButton);
        updateButton = findViewById(R.id.updateButton);

        db = FirebaseFirestore.getInstance();

        String postId = getIntent().getStringExtra("postID");
        //load
        db.collection("posts")
                .whereEqualTo("id", postId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                PostAI post = document.toObject(PostAI.class);
                                documentId = document.getId();
                                displayDetailPost(post);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        //Reject
        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updatePostStatus("rejected");
                Toast.makeText(v.getContext(), "Bài viết đã bị từ chối", Toast.LENGTH_SHORT).show();
            }
        });

        //Accept
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePostStatus("approved");
                Toast.makeText(v.getContext(), "Bài viết đã được chấp nhận", Toast.LENGTH_SHORT).show();
            }
        });
//
//        updateButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(DetailPostAIActivity.this, UpdatePostAIActivity.class);
//                intent.putExtra("postId", postId);
//                startActivity(intent);
//            }
//        });
    }

    private void displayDetailPost(PostAI post) {
        String img = post.getImg();

        byte[] bytes= Base64.decode(img,Base64.DEFAULT);

        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);

        Glide.with(this)
                .load(bitmap)
                .error(R.drawable.upload)
                .into(detailImage);

        detailTitle.setText(post.getTitle());
        content.setText(post.getContent());
        createdAt.setText(DATE_FORMAT.format(post.getCreatedAt().toDate()));
        updatedAt.setText(DATE_FORMAT.format(post.getUpdatedAt().toDate()));
    }

    private void updatePostStatus(String status) {
        DocumentReference postRef = db.collection("posts").document(documentId);
        postRef.update("status", status)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
    }
}
