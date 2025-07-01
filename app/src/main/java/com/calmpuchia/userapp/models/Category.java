package com.calmpuchia.userapp.models;

public class Category {
    private String categoryId;
    private String name;
    private String iconUrl;

    // Constructor
    public Category() {
        // Required empty constructor for Firestore
    }

    public Category(String categoryId, String name, String iconUrl) {
        this.categoryId = categoryId;
        this.name = name;
        this.iconUrl = iconUrl;
    }

    // Getters and Setters
    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}