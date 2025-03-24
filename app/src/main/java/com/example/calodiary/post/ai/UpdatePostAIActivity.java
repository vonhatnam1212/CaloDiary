package com.example.calodiary.post.ai;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.calodiary.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class UpdatePostAIActivity extends AppCompatActivity {
    private TextView updateTitle, updateContent;
    private ImageView updateImage;
    private Button updateButton;
    private FirebaseFirestore db;
    private String documentId;
    private Uri selectedImageUri;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_post_ai);

        updateButton = findViewById(R.id.updateButton);
        updateImage = findViewById(R.id.updateImage);
        updateTitle = findViewById(R.id.updateTitle);
        updateContent = findViewById(R.id.updateContent);

        db = FirebaseFirestore.getInstance();
        String postId = getIntent().getStringExtra("postId");

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            selectedImageUri = data.getData();
                            updateImage.setImageURI(selectedImageUri);
                        }
                    }
                }
        );


        updateImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPicker = new Intent(Intent.ACTION_GET_CONTENT);
                photoPicker.setType("image/*");
                activityResultLauncher.launch(photoPicker);
            }
        });



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
    }

    private void displayDetailPost(PostAI post) {
        String img = post.getImg();

        byte[] bytes= Base64.decode(img,Base64.DEFAULT);

        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);

        Glide.with(this)
                .load(bitmap)
                .error(R.drawable.upload)
                .into(updateImage);

        updateTitle.setText(post.getTitle());
        updateContent.setText(post.getContent());

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String content = updateContent.getText().toString().trim();
                String title = updateTitle.getText().toString().trim();
                post.setContent(content);
                post.setTitle(title);
                post.setUpdatedAt(new Timestamp(new java.util.Date()));

                if (selectedImageUri != null) {
                    try {
                        Bitmap bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),selectedImageUri);

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();

                        bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);

                        byte[] bytes=stream.toByteArray();

                        String imgPath = Base64.encodeToString(bytes,Base64.DEFAULT);
                        post.setImg(imgPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


                if (content.isEmpty() || title.isEmpty()) {
                    Toast.makeText(UpdatePostAIActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                DocumentReference postRef = db.collection("posts").document(documentId);
                postRef.set(post)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully updated!");
                                Intent intent = new Intent(UpdatePostAIActivity.this, ListPostAIActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(UpdatePostAIActivity.this, "Failed to update post", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }
}