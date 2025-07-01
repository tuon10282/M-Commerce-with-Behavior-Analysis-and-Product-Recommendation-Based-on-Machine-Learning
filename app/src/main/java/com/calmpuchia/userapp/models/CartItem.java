package com.calmpuchia.userapp.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class CartItem implements Parcelable {
    private String productID;
    private String name;
    private String imageUrl;
    private int price; // Stored as cents (price * 100)
    private int quantity;
    private int totalPrice;
    private boolean selected;
    private String userId;
    private boolean hasDiscount;
    private int discountPrice;

    // Default constructor
    public CartItem() {}

    // Constructor with int price
    public CartItem(String productID, String name, String imageUrl, int price, int quantity) {
        Log.d("CartItem", "Creating CartItem with int price: " + productID + ", " + name + ", " + price);
        this.productID = productID;
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.quantity = quantity;
        this.totalPrice = price * quantity;
        this.selected = false;
        Log.d("CartItem", "CartItem created successfully: " + this.toString());
    }

    // Constructor with double price (converts to cents)
    public CartItem(String productID, String name, String imageUrl, double price, int quantity) {
        Log.d("CartItem", "Creating CartItem with double price: " + productID + ", " + name + ", " + price);
        this.productID = productID;
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = (int) Math.round(price * 100); // Convert to cents
        this.quantity = quantity;
        this.totalPrice = this.price * quantity;
        this.selected = false;
        Log.d("CartItem", "CartItem created successfully: " + this.toString());
    }

    // Static factory method to create from Products object
    public static CartItem fromProduct(Products product, int quantity) {
        if (product == null) {
            Log.e("CartItem", "Cannot create CartItem from null Product");
            return null;
        }

        Log.d("CartItem", "Creating CartItem from Product:");
        Log.d("CartItem", "- ID: " + product.getProduct_id());
        Log.d("CartItem", "- Name: " + product.getName());
        Log.d("CartItem", "- Image URL: " + product.getImage_url());
        Log.d("CartItem", "- Price: " + product.getPrice());

        CartItem cartItem = new CartItem(
                product.getProduct_id(),
                product.getName(),
                product.getImage_url(),
                product.getPrice(), // Uses the double constructor
                quantity
        );

        // Set discount if available
        if (product.getDiscount_price() > 0) {
            cartItem.setDiscountPrice((int) Math.round(product.getDiscount_price() * 100));
        }

        return cartItem;
    }

    // Parcelable implementation
    protected CartItem(Parcel in) {
        productID = in.readString();
        name = in.readString();
        imageUrl = in.readString();
        price = in.readInt();
        quantity = in.readInt();
        totalPrice = in.readInt();
        selected = in.readByte() != 0;
        userId = in.readString();
        hasDiscount = in.readByte() != 0;
        discountPrice = in.readInt();
    }

    public static final Creator<CartItem> CREATOR = new Creator<CartItem>() {
        @Override
        public CartItem createFromParcel(Parcel in) {
            return new CartItem(in);
        }

        @Override
        public CartItem[] newArray(int size) {
            return new CartItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(productID);
        dest.writeString(name);
        dest.writeString(imageUrl);
        dest.writeInt(price);
        dest.writeInt(quantity);
        dest.writeInt(totalPrice);
        dest.writeByte((byte) (selected ? 1 : 0));
        dest.writeString(userId);
        dest.writeByte((byte) (hasDiscount ? 1 : 0));
        dest.writeInt(discountPrice);
    }

    // Getters and Setters
    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getPrice() {
        return price;
    }

    // Get price as double (convert from cents)
    public double getPriceAsDouble() {
        return price / 100.0;
    }

    public void setPrice(int price) {
        this.price = price;
        updateTotalPrice();
    }

    // Set price from double (converts to cents)
    public void setPriceFromDouble(double price) {
        this.price = (int) Math.round(price * 100);
        updateTotalPrice();
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        updateTotalPrice();
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    // Get total price as double (convert from cents)
    public double getTotalPriceAsDouble() {
        return totalPrice / 100.0;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isHasDiscount() {
        return hasDiscount;
    }

    public void setHasDiscount(boolean hasDiscount) {
        this.hasDiscount = hasDiscount;
    }

    public int getDiscountPrice() {
        return discountPrice;
    }

    // Get discount price as double (convert from cents)
    public double getDiscountPriceAsDouble() {
        return discountPrice / 100.0;
    }

    public void setDiscountPrice(int discountPrice) {
        this.discountPrice = discountPrice;
        this.hasDiscount = discountPrice > 0;
    }

    // Set discount price from double (converts to cents)
    public void setDiscountPriceFromDouble(double discountPrice) {
        this.discountPrice = (int) Math.round(discountPrice * 100);
        this.hasDiscount = this.discountPrice > 0;
    }

    // Helper method to update total price
    private void updateTotalPrice() {
        this.totalPrice = this.price * this.quantity;
    }

    // Helper method to get effective price (discount price if available, otherwise regular price)
    public int getEffectivePrice() {
        return hasDiscount && discountPrice > 0 ? discountPrice : price;
    }

    // Get effective price as double
    public double getEffectivePriceAsDouble() {
        return getEffectivePrice() / 100.0;
    }

    // Helper method to calculate total with effective price
    public int getEffectiveTotalPrice() {
        return getEffectivePrice() * quantity;
    }

    // Get effective total price as double
    public double getEffectiveTotalPriceAsDouble() {
        return getEffectiveTotalPrice() / 100.0;
    }

    // Validation method
    public boolean isValid() {
        return productID != null && !productID.isEmpty() &&
                name != null && !name.isEmpty() &&
                price > 0 &&
                quantity > 0;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "productID='" + productID + '\'' +
                ", name='" + name + '\'' +
                ", price=" + getPriceAsDouble() +
                ", quantity=" + quantity +
                ", totalPrice=" + getTotalPriceAsDouble() +
                ", selected=" + selected +
                ", hasDiscount=" + hasDiscount +
                ", discountPrice=" + getDiscountPriceAsDouble() +
                '}';
    }
}