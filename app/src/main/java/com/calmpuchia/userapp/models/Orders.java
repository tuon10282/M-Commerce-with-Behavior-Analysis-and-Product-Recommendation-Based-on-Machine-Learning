package com.calmpuchia.userapp.models;

public class Orders {
    private String order_id;
    private String created_at;
    private String status; // 'pending' | 'confirmed' | 'shipped' | 'delivered' | 'cancelled'
    private String store_id;
    private String buy_at; // 'online store' | 'offline store'
    private String customer_name;
    private String customer_phone;
    private String customer_address;
    private String payment_method;
    private double shipping_fee;
    private double total;
    private String user_id;
    private String voucher_code;
    private Double voucher_discount;

    // Constructor mặc định
    public Orders() {}

    // Constructor đầy đủ (tương tự TypeScript constructor)
    public Orders(String order_id, String created_at, String status, String store_id,
                 String buy_at, String customer_name, String customer_phone,
                 String customer_address, String payment_method, double shipping_fee,
                 double total, String user_id) {
        this.order_id = order_id;
        this.created_at = created_at;
        this.status = status;
        this.store_id = store_id;
        this.buy_at = buy_at;
        this.customer_name = customer_name;
        this.customer_phone = customer_phone;
        this.customer_address = customer_address;
        this.payment_method = payment_method;
        this.shipping_fee = shipping_fee;
        this.total = total;
        this.user_id = user_id;
    }

    // Constructor với voucher (optional parameters)
    public Orders(String order_id, String created_at, String status, String store_id,
                 String buy_at, String customer_name, String customer_phone,
                 String customer_address, String payment_method, double shipping_fee,
                 double total, String user_id, String voucher_code, Double voucher_discount) {
        this(order_id, created_at, status, store_id, buy_at, customer_name, customer_phone,
                customer_address, payment_method, shipping_fee, total, user_id);
        this.voucher_code = voucher_code;
        this.voucher_discount = voucher_discount;
    }

    // Getters and Setters
    public String getOrder_id() { return order_id; }
    public void setOrder_id(String order_id) { this.order_id = order_id; }

    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        // Validation for status values
        if (status != null && (status.equals("pending") || status.equals("confirmed") ||
                status.equals("shipped") || status.equals("delivered") || status.equals("cancelled"))) {
            this.status = status;
        } else {
            throw new IllegalArgumentException("Invalid status value");
        }
    }

    public String getStore_id() { return store_id; }
    public void setStore_id(String store_id) { this.store_id = store_id; }

    public String getBuy_at() { return buy_at; }
    public void setBuy_at(String buy_at) {
        // Validation for buy_at values
        if (buy_at != null && (buy_at.equals("online store") || buy_at.equals("offline store"))) {
            this.buy_at = buy_at;
        } else {
            throw new IllegalArgumentException("Invalid buy_at value");
        }
    }

    public String getCustomer_name() { return customer_name; }
    public void setCustomer_name(String customer_name) { this.customer_name = customer_name; }

    public String getCustomer_phone() { return customer_phone; }
    public void setCustomer_phone(String customer_phone) { this.customer_phone = customer_phone; }

    public String getCustomer_address() { return customer_address; }
    public void setCustomer_address(String customer_address) { this.customer_address = customer_address; }

    public String getPayment_method() { return payment_method; }
    public void setPayment_method(String payment_method) { this.payment_method = payment_method; }

    public double getShipping_fee() { return shipping_fee; }
    public void setShipping_fee(double shipping_fee) { this.shipping_fee = shipping_fee; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getUser_id() { return user_id; }
    public void setUser_id(String user_id) { this.user_id = user_id; }

    public String getVoucher_code() { return voucher_code; }
    public void setVoucher_code(String voucher_code) { this.voucher_code = voucher_code; }

    public Double getVoucher_discount() { return voucher_discount; }
    public void setVoucher_discount(Double voucher_discount) { this.voucher_discount = voucher_discount; }

    // Utility methods
    public boolean isDelivered() {
        return "delivered".equals(status);
    }

    public boolean isPending() {
        return "pending".equals(status);
    }

    public boolean isConfirmed() {
        return "confirmed".equals(status);
    }

    public boolean isShipped() {
        return "shipped".equals(status);
    }

    public boolean isCancelled() {
        return "cancelled".equals(status);
    }

    public boolean hasVoucher() {
        return voucher_code != null && !voucher_code.trim().isEmpty();
    }

    public boolean isOnlineOrder() {
        return "online store".equals(buy_at);
    }

    public boolean isOfflineOrder() {
        return "offline store".equals(buy_at);
    }
}