package com.calmpuchia.userapp.models;

import java.util.List;

public class ProductReview {
    private String feedbackId;
    private String orderId;
    private String productId;
    private String userId;
    private String content;
    private int ratings;
    private List<String> images;
    private String createdAt;

    // Constructor mặc định
    public ProductReview() {}

    // Constructor đầy đủ
    public ProductReview(String feedbackId, String orderId, String productId,
                         String userId, String content, int ratings,
                         List<String> images, String createdAt) {
        this.feedbackId = feedbackId;
        this.orderId = orderId;
        this.productId = productId;
        this.userId = userId;
        this.content = content;
        this.ratings = ratings;
        this.images = images;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getFeedbackId() { return feedbackId; }
    public void setFeedbackId(String feedbackId) { this.feedbackId = feedbackId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getRatings() { return ratings; }
    public void setRatings(int ratings) { this.ratings = ratings; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isValid() {
        return content != null && !content.trim().isEmpty()
                && ratings > 0 && ratings <= 5
                && productId != null && userId != null;
    }

}
