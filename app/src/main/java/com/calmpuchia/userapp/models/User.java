package com.calmpuchia.userapp.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

public class User {
    @DocumentId
    private String userId;
    private String name;
    private String phone;
    private String email;
    private String password;
    private String dob;
    private String gender;
    private String address;
    private String image;
    private int loyaltyPoints;
    @ServerTimestamp
    private Timestamp createdAt;
    @ServerTimestamp
    private Timestamp lastLogin;
    private boolean emailVerified;


    // Constructor rỗng bắt buộc cho Firestore
    public User() {}
    public User(String name, String phone, String email, String password) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.emailVerified = false; // Mặc định chưa verify
    }


    public User(String userId, String name, String phone, String email, String password, String dob, String gender, String address, String image, int loyaltyPoints, Timestamp createdAt, Timestamp lastLogin, boolean emailVerified) {
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.dob = dob;
        this.gender = gender;
        this.address = address;
        this.image = image;
        this.loyaltyPoints = loyaltyPoints;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
        this.emailVerified = emailVerified;
    }

    // Constructor cho đăng ký


    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }

    public void setLoyaltyPoints(int loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
}

