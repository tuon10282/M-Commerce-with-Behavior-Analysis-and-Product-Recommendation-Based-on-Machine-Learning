package com.calmpuchia.userapp.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProductsRec {
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
    private String updated_at;  // Changed to String
    private String created_at;  // Changed to String

    // Constructor không tham số
    public ProductsRec() {}

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

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    // Helper methods to convert string to Date
    public Date getCreatedAtAsDate() {
        return parseStringToDate(created_at);
    }

    public Date getUpdatedAtAsDate() {
        return parseStringToDate(updated_at);
    }

    // Helper method to parse various date formats
    private Date parseStringToDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        // Common date formats from APIs
        String[] formats = {
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ssXXX"
        };

        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                return sdf.parse(dateString);
            } catch (ParseException e) {
                // Try next format
            }
        }

        return null; // Return null if no format matches
    }

    // Helper method to format date as string
    public String getFormattedCreatedAt() {
        Date date = getCreatedAtAsDate();
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return sdf.format(date);
        }
        return "";
    }

    public String getFormattedUpdatedAt() {
        Date date = getUpdatedAtAsDate();
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return sdf.format(date);
        }
        return "";
    }
}