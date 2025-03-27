package com.example.calodiary.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class User {
    private String uid;                  // Không có trong schema nhưng giữ lại vì là ID từ Firestore
    private String email;                // varchar(255) [unique]
    private String fullName;             // varchar(255)
    private String username;             // varchar(255) [unique]
    private String dob;                  // String (định dạng chuỗi ngày sinh)
    private String gender;               // enum('male', 'female')
    private float height;                // float
    private float weight;                // float
    private String img;                  // URL của avatar, không có trong schema nhưng giữ lại
    private Date updatedAt;              // Không có trong schema nhưng giữ lại cho Firestore
    private String role;                 // enum('user', 'admin') [default: 'user']
    private String pendingEmail;         // Không có trong schema nhưng giữ lại
    private boolean emailVerified;       // Không có trong schema nhưng giữ lại
    private String password;             // varchar(255), thêm vào từ schema

    // Constructor mặc định (yêu cầu bởi Firestore)
    public User() {
    }

    // Constructor đầy đủ
    public User(String uid, String email, String fullName, String username, String dob,
                String gender, float height, float weight, String img, Date updatedAt,
                String role, String pendingEmail, boolean emailVerified, String password) {
        this.uid = uid;
        this.email = email;
        this.fullName = fullName;
        this.username = username;
        this.dob = dob;
        this.gender = gender;
        this.height = height;
        this.weight = weight;
        this.img = img;
        this.updatedAt = updatedAt;
        this.role = role;
        this.pendingEmail = pendingEmail;
        this.emailVerified = emailVerified;
        this.password = password;
    }

    // Getters và Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPendingEmail() {
        return pendingEmail;
    }

    public void setPendingEmail(String pendingEmail) {
        this.pendingEmail = pendingEmail;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Phương thức chuyển đổi thành Map để lưu vào Firestore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (email != null) map.put("email", email);
        if (fullName != null) map.put("full_name", fullName);
        if (username != null) map.put("username", username);
        if (dob != null) map.put("dob", dob);
        if (gender != null) map.put("gender", gender);
        map.put("height", height);
        map.put("weight", weight);
        if (img != null) map.put("img", img);
        if (updatedAt != null) map.put("updated_at", updatedAt);
        if (role != null) map.put("role", role);
        if (pendingEmail != null) map.put("pendingEmail", pendingEmail);
        map.put("emailVerified", emailVerified);
        if (password != null) map.put("password", password); // Thêm password vào map
        return map;
    }

    // Phương thức khởi tạo từ DocumentSnapshot
    public static User fromDocument(Map<String, Object> data, String uid) {
        User user = new User();
        user.setUid(uid);
        user.setEmail((String) data.get("email"));
        user.setFullName((String) data.get("full_name"));
        user.setUsername((String) data.get("username"));
        user.setDob((String) data.get("dob"));
        user.setGender((String) data.get("gender"));

        // Xử lý height
        Object heightValue = data.get("height");
        if (heightValue != null) {
            if (heightValue instanceof Double) {
                user.setHeight(((Double) heightValue).floatValue());
            } else if (heightValue instanceof String) {
                try {
                    user.setHeight(Float.parseFloat((String) heightValue));
                } catch (NumberFormatException e) {
                    user.setHeight(0f);
                    e.printStackTrace();
                }
            } else {
                user.setHeight(0f);
            }
        } else {
            user.setHeight(0f);
        }

        // Xử lý weight
        Object weightValue = data.get("weight");
        if (weightValue != null) {
            if (weightValue instanceof Double) {
                user.setWeight(((Double) weightValue).floatValue());
            } else if (weightValue instanceof String) {
                try {
                    user.setWeight(Float.parseFloat((String) weightValue));
                } catch (NumberFormatException e) {
                    user.setWeight(0f);
                    e.printStackTrace();
                }
            } else {
                user.setWeight(0f);
            }
        } else {
            user.setWeight(0f);
        }

        user.setImg((String) data.get("img"));
        user.setUpdatedAt((Date) data.get("updated_at"));
        user.setRole((String) data.get("role"));
        user.setPendingEmail((String) data.get("pendingEmail"));
        user.setEmailVerified(data.get("emailVerified") != null && (Boolean) data.get("emailVerified"));
        user.setPassword((String) data.get("password")); // Thêm password từ data

        return user;
    }
}