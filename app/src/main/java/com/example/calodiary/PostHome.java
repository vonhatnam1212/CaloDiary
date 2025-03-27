package com.example.calodiary;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.util.List;


public class PostHome implements Serializable {
    private boolean isLiked = false; // Mặc định chưa like
    private String id;
    private String title;
    private String content;
    private String authorId;
    private String status; // 'pending', 'approved', 'rejected'
    private String img;
    private boolean isAI;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private int likeCount;
    private int commentCount;
    private List<Comment> comments;
    public PostHome(String title, String content, String authorId, String status, String img, boolean isAI, Timestamp createdAt, Timestamp updatedAt, int likeCount, int commentCount, List<Comment> comments) {
        this.title = title;
        this.content = content;
        this.authorId = authorId;
        this.status = status;
        this.img = img;
        this.isAI = isAI;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.comments = comments;
    }

    public PostHome() {
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

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isAI() {
        return isAI;
    }

    public void setAI(boolean AI) {
        isAI = AI;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }
    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }
}