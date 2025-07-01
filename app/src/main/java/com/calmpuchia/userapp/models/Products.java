package com.calmpuchia.userapp.models;

import com.google.firebase.Timestamp;
import java.util.Date;
import java.util.List;

public class Products {
    private String product_id;
    private String name;
    private List<String> description;
    private String brand;
    private String image_url;
    private double price;
    private double discount_price;
    private int stock;
    private String category_id;
    private List<String> tags;
    private Timestamp updated_at;  // Sử dụng Timestamp
    private Timestamp created_at;  // Sử dụng Timestamp
    private String store_id;  // Sử dụng Timestamp

    // Constructor không tham số
    public Products() {}

    // Getters and Setters
    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getDiscount_price() {
        return discount_price;
    }

    public void setDiscount_price(double discount_price) {
        this.discount_price = discount_price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Timestamp getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    public Timestamp getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(Timestamp updated_at) {
        this.updated_at = updated_at;
    }

    // Helper method to convert to java.util.Date if needed
    public Date getCreatedAtAsDate() {
        return created_at != null ? created_at.toDate() : null;
    }

    public Date getUpdatedAtAsDate() {
        return updated_at != null ? updated_at.toDate() : null;
    }

    // Helper method to format date as string (if you want)
    public String getFormattedCreatedAt() {
        return created_at != null ? created_at.toDate().toString() : "";
    }

    public String getStore_id() {
        return store_id;
    }

    public void setStore_id(String store_id) {
        this.store_id = store_id;
    }

    public String getFormattedUpdatedAt() {
        return updated_at != null ? updated_at.toDate().toString() : "";

    }
    // Utility methods
    public boolean hasDiscount() {
        return  discount_price > 0 && discount_price < price;
    }

    public double getDiscountPercentage() {
        if (hasDiscount()) {
            return ((price - discount_price) / price) * 100;
        }
        return 0;
    }

    public Double getFinalPrice() {
        return hasDiscount() ? discount_price : price;
    }

    // Helper method to get the correct store ID regardless of field name
    public String getEffectiveStoreId() {
        return store_id != null ? store_id : store_id;
    }

    // Helper method to get the correct product ID regardless of field name
    public String getEffectiveProductId() {
        return product_id != null ? product_id : product_id;
    }

    // Helper method to get updated_at as a formatted string
    public String getUpdatedAtString() {
        if (updated_at != null) {
            return new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm:ss", java.util.Locale.getDefault())
                    .format(updated_at.toDate());
        }
        return "";
    }

    // Helper method to get updated_at as Date
    public java.util.Date getUpdatedAtDate() {
        return updated_at != null ? updated_at.toDate() : null;
    }

}
