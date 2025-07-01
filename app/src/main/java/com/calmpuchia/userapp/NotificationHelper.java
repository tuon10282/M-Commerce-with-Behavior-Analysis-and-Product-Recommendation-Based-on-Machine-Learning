package com.calmpuchia.userapp;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class NotificationHelper {
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Tạo notification đơn giản
    public static void sendNotification(String userId, String title, String message, String type) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", userId);
        notification.put("title", title);
        notification.put("message", message);
        notification.put("type", type);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("read", false);

        db.collection("notifications").add(notification);
    }

    // Notification cho đơn hàng
    public static void sendOrderNotification(String userId, String orderId, String status) {
        String title = getOrderTitle(status);
        String message = getOrderMessage(status, orderId);

        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", userId);
        notification.put("title", title);
        notification.put("message", message);
        notification.put("type", "order");
        notification.put("orderId", orderId);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("read", false);

        db.collection("notifications").add(notification);
    }

    // Notification cho khuyến mãi
    public static void sendPromotionNotification(String userId, String productName, String discount) {
        String title = "🎉 Khuyến mãi hot!";
        String message = productName + " đang giảm giá " + discount + "%";

        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", userId);
        notification.put("title", title);
        notification.put("message", message);
        notification.put("type", "promotion");
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("read", false);

        db.collection("notifications").add(notification);
    }

    private static String getOrderTitle(String status) {
        switch (status) {
            case "confirmed": return "✅ Đơn hàng đã xác nhận";
            case "preparing": return "👨‍🍳 Đang chuẩn bị";
            case "shipping": return "🚚 Đang giao hàng";
            case "delivered": return "📦 Đã giao thành công";
            case "cancelled": return "❌ Đơn hàng bị hủy";
            default: return "📋 Cập nhật đơn hàng";
        }
    }

    private static String getOrderMessage(String status, String orderId) {
        switch (status) {
            case "confirmed": return "Đơn hàng #" + orderId + " đã được xác nhận và đang xử lý";
            case "preparing": return "Đơn hàng #" + orderId + " đang được chuẩn bị";
            case "shipping": return "Đơn hàng #" + orderId + " đã được giao cho shipper";
            case "delivered": return "Đơn hàng #" + orderId + " đã được giao thành công";
            case "cancelled": return "Đơn hàng #" + orderId + " đã bị hủy";
            default: return "Đơn hàng #" + orderId + " có cập nhật mới";
        }
    }
}