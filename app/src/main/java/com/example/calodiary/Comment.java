package com.example.calodiary;

import java.io.Serializable;

public class Comment implements Serializable {
    private String username;
    private String text;
    private int avatarResId; // Nếu có ảnh đại diện

    // Constructor mặc định (bắt buộc cho Firebase)
    public Comment() {
        // Khởi tạo giá trị mặc định để tránh lỗi null
        this.username = "";
        this.text = "";
        this.avatarResId = 0;
    }

    // Constructor có tham số
    public Comment(String username, String text, int avatarResId) {
        this.username = username;
        this.text = text;
        this.avatarResId = avatarResId;
    }

    public String getUsername() { return username; }
    public String getText() { return text; }
    public int getAvatarResId() { return avatarResId; }
}
