package com.calmpuchia.userapp.models;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;

@IgnoreExtraProperties
public class Store {
    @PropertyName("store_id")
    private String storeId;

    private String name;
    private String address;
    private double lat;
    private double lng;
    private String phoneNumber;
    private String image;

    @PropertyName("is_active")
    private boolean isActive;

    // Default constructor for Firebase
    public Store() {}

    public Store(String storeId, String name, String address, double lat, double lng, boolean isActive) {
        this.storeId = storeId;
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.isActive = isActive;
    }

    // Getter + Setter with @PropertyName if needed

    @PropertyName("store_id")
    public String getStoreId() {
        return storeId;
    }

    @PropertyName("store_id")
    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    // REMOVED: The Object parameter version that was causing the conflict
    // public void setLat(Object lat) { ... }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    // REMOVED: The Object parameter version that was causing the conflict
    // public void setLng(Object lng) { ... }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @PropertyName("is_active")
    public boolean isActive() {
        return isActive;
    }

    @PropertyName("is_active")
    public void setActive(boolean active) {
        isActive = active;
    }

    // Convenience method
    public boolean hasValidCoordinates() {
        return lat != 0.0 && lng != 0.0;
    }
}