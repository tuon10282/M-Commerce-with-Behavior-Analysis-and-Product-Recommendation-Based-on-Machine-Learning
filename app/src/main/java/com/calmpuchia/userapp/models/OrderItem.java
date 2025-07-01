package com.calmpuchia.userapp.models;

public class OrderItem {
    private String product_id;
    private String product_name;
    private String product_image;
    private int quantity;
    private double price;
    private boolean reviewed;

    // Constructor mặc định
    public OrderItem() {}

    // Constructor đầy đủ
    public OrderItem(String product_id, String product_name, String product_image,
                     int quantity, double price, boolean reviewed) {
        this.product_id = product_id;
        this.product_name = product_name;
        this.product_image = product_image;
        this.quantity = quantity;
        this.price = price;
        this.reviewed = reviewed;
    }

    // Constructor không có reviewed (mặc định false)
    public OrderItem(String product_id, String product_name, String product_image,
                     int quantity, double price) {
        this(product_id, product_name, product_image, quantity, price, false);
    }

    // Getters and Setters
    public String getProduct_id() { return product_id; }
    public void setProduct_id(String product_id) { this.product_id = product_id; }

    public String getProduct_name() { return product_name; }
    public void setProduct_name(String product_name) { this.product_name = product_name; }

    public String getProduct_image() { return product_image; }
    public void setProduct_image(String product_image) { this.product_image = product_image; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public boolean isReviewed() { return reviewed; }
    public void setReviewed(boolean reviewed) { this.reviewed = reviewed; }

    // Utility methods
    public double getTotalPrice() {
        return price * quantity;
    }

    public boolean hasValidQuantity() {
        return quantity > 0;
    }

    public boolean hasValidPrice() {
        return price > 0;
    }

    public boolean isValidItem() {
        return product_id != null && !product_id.trim().isEmpty() &&
                product_name != null && !product_name.trim().isEmpty() &&
                hasValidQuantity() && hasValidPrice();
    }
}