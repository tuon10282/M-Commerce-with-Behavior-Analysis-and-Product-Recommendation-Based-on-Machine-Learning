package com.calmpuchia.userapp.models;

import java.util.Date;
import java.util.List;

public class Products {
    private String product_id;
    private String name;
    private List<String> description;  // Sửa từ String thành List<String>
    private String brand;
    private String image_url;
    private double price;
    private double discount_price;
    private int stock;
    private String category_id;        // giữ String như bạn đã sửa
    private List<String> tags;         // Sửa từ String thành List<String>
    private Date updated_at;
    private Date created_at;

    // Constructor không tham số - RẤT QUAN TRỌNG cho Firestore
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

    public Date getCreated_at() {
        return created_at;
    }
    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Date getUpdated_at() {
        return updated_at;
    }
    public void setUpdated_at(Date updated_at) {
        this.updated_at = updated_at;
    }
}
