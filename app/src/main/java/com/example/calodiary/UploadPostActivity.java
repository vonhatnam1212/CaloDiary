package com.example.calodiary;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.calodiary.Model.Posts;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class UploadPostActivity extends AppCompatActivity {
    private ImageView uploadImage;
    private EditText uploadTitle;
    private EditText uploadContent;
    private Button saveButton;
    private Uri selectedImageUri;
    private FirebaseFirestore db;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upload_post);

        uploadImage = findViewById(R.id.uploadImage);
        uploadTitle = findViewById(R.id.uploadTitle);
        uploadContent = findViewById(R.id.uploadContent);
        saveButton = findViewById(R.id.saveButton);

        db = FirebaseFirestore.getInstance();

        ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            selectedImageUri = data.getData();
                            uploadImage.setImageURI(selectedImageUri);
                        }
                    }
                }
        );


        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPicker = new Intent(Intent.ACTION_GET_CONTENT);
                photoPicker.setType("image/*");
                activityResultLauncher.launch(photoPicker);
            }
        });

        // Save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedImageUri != null) {
                    savePost();
                } else {
                    Toast.makeText(UploadPostActivity.this, "Please select image", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void savePost() {
        String title = uploadTitle.getText().toString().trim();
        String content = uploadContent.getText().toString().trim();
        String imgPath = selectedImageUri != null ? selectedImageUri.toString() : null;
        //
        //
        String authorId = "1";

        String status = "pending";
        Date currentTime = new Date();
        String createdAtString = DATE_FORMAT.format(currentTime);
        String updatedAtString = DATE_FORMAT.format(currentTime);


        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

            byte[] bytes = stream.toByteArray();

            imgPath = Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Posts newPost = new Posts(title, content, authorId, status, imgPath, createdAtString, updatedAtString);
        newPost.setId(UUID.randomUUID().toString());

        db.collection("posts")
                .add(newPost)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {

                        Toast.makeText(UploadPostActivity.this, "Post saved successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UploadPostActivity.this, DisplayCreatedPostActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UploadPostActivity.this, "Failed to save post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}