package com.calmpuchia.userapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "notification_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            Log.d(TAG, "Message Notification Title: " + title);
            Log.d(TAG, "Message Notification Body: " + body);

            // Show notification
            showNotification(title, body);

            // Save to Firestore if user is logged in
            saveNotificationToFirestore(title, body, "general");
        }

        // Check if message contains data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            String title = remoteMessage.getData().get("title");
            String message = remoteMessage.getData().get("message");
            String type = remoteMessage.getData().get("type");

            if (title != null && message != null) {
                showNotification(title, message);
                saveNotificationToFirestore(title, message, type != null ? type : "general");
            }
        }
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        // Send token to server if needed
        sendTokenToServer(token);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notification Channel";
            String description = "Channel for app notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(String title, String message) {
        Intent intent = new Intent(this, NotificationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_noti)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

    private void saveNotificationToFirestore(String title, String message, String type) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Log.d(TAG, "User not logged in, not saving notification");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", userId);
        notification.put("title", title);
        notification.put("message", message);
        notification.put("type", type);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("read", false);

        db.collection("notifications")
                .add(notification)
                .addOnSuccessListener(documentReference ->
                        Log.d(TAG, "Notification saved with ID: " + documentReference.getId()))
                .addOnFailureListener(e ->
                        Log.w(TAG, "Error adding notification", e));
    }

    private void sendTokenToServer(String token) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Log.d(TAG, "User not logged in, not saving token");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("token", token);
        tokenData.put("timestamp", System.currentTimeMillis());

        db.collection("users")
                .document(userId)
                .update("fcmToken", token)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "FCM token updated successfully"))
                .addOnFailureListener(e ->
                        Log.w(TAG, "Error updating FCM token", e));
    }

    // Static methods for creating notifications programmatically
    public static void createOrderNotification(String userId, String orderId, String status) {
        if (userId == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", userId);
        notification.put("title", getOrderTitle(status));
        notification.put("message", getOrderMessage(status, orderId));
        notification.put("type", "order");
        notification.put("orderId", orderId);
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("read", false);

        db.collection("notifications").add(notification);
    }

    public static void createPromotionNotification(String userId, String productName, String discount) {
        if (userId == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", userId);
        notification.put("title", "🎉 Khuyến mãi hot!");
        notification.put("message", productName + " đang giảm giá " + discount + "%");
        notification.put("type", "promotion");
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("read", false);

        db.collection("notifications").add(notification);
    }

    private static String getOrderTitle(String status) {
        switch (status) {
            case "confirmed": return "✅ Order Has Been Confirm";
            case "preparing": return "👨‍🍳 Đang chuẩn bị";
            case "shipping": return "🚚 Đang giao hàng";
            case "delivered": return "📦 Đã giao thành công";
            case "cancelled": return "❌ Đơn hàng bị hủy";
            default: return "📋 Cập nhật đơn hàng";
        }
    }

    private static String getOrderMessage(String status, String orderId) {
        switch (status) {
            case "confirmed": return "Đơn hàng #" + orderId + " has been confirm";
            case "preparing": return "Đơn hàng #" + orderId + " đang được chuẩn bị";
            case "shipping": return "Đơn hàng #" + orderId + " đang trên đường giao";
            case "delivered": return "Đơn hàng #" + orderId + " đã được giao thành công";
            case "cancelled": return "Đơn hàng #" + orderId + " đã bị hủy";
            default: return "Đơn hàng #" + orderId + " có cập nhật mới";
        }
    }
}