package com.example.calodiary.Model;

import java.io.Serializable;


public class Posts implements Serializable {

    private String id;
    private String title;
    private String content;
    private String authorId;
    private String status; // 'pending', 'approved', 'rejected'
    private String img;
    private String createdAt;
    private String updatedAt;

    public Posts(String title, String content, String authorId, String status, String img, String createdAt, String updatedAt) {
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.status = status;
        this.img = img;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Posts() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
