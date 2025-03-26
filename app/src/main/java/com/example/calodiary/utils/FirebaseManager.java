package com.example.calodiary.utils;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.example.calodiary.model.User;

import java.util.function.Consumer;

public class FirebaseManager {
    private static FirebaseManager instance;
    public final FirebaseAuth mAuth;
    public final FirebaseFirestore db;
    private final FirebaseStorage storage;

    private FirebaseManager() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public static FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void updateUserProfile(FirebaseUser user, User userData, Context context, Runnable onSuccess) {
        db.collection("users").document(user.getUid())
                .update(userData.toMap())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Cập nhật hồ sơ thành công", Toast.LENGTH_SHORT).show();
                    onSuccess.run();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Lỗi khi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public void setUserProfile(FirebaseUser user, User userData, Context context, Runnable onSuccess) {
        db.collection("users").document(user.getUid())
                .set(userData.toMap())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Lưu hồ sơ thành công", Toast.LENGTH_SHORT).show();
                    onSuccess.run();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Lỗi khi lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public void uploadAvatar(FirebaseUser user, Uri imageUri, Consumer<String> onSuccess, Context context) {
        StorageReference fileRef = storage.getReference().child("avatars/" + user.getUid() + ".jpg");
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> onSuccess.accept(uri.toString())))
                .addOnFailureListener(e -> Toast.makeText(context, "Lỗi khi upload avatar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public void checkEmailAvailability(String email, Runnable onAvailable, Context context) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().isEmpty()) {
                        onAvailable.run();
                    } else {
                        Toast.makeText(context, "Email đã được sử dụng!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void checkUsernameAvailability(String username, Runnable onAvailable, Context context) {
        db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().isEmpty()) {
                        onAvailable.run();
                    } else {
                        Toast.makeText(context, "Username đã được sử dụng!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void sendPasswordResetEmail(String email, Context context, Runnable onSuccess) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(context, "Email đặt lại mật khẩu đã được gửi!", Toast.LENGTH_LONG).show();
                        onSuccess.run();
                    } else {
                        Toast.makeText(context, "Lỗi khi gửi email: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void signOut() {
        mAuth.signOut();
    }

    public void loadUserData(FirebaseUser user, Consumer<User> onSuccess, Context context) {
        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        User userData = User.fromDocument(document.getData(), user.getUid());
                        onSuccess.accept(userData);
                    } else {
                        Toast.makeText(context, "Không tìm thấy dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Lỗi khi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}