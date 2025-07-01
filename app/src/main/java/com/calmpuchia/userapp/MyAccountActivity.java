package com.calmpuchia.userapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyAccountActivity extends AppCompatActivity {

    private TextView txtName, txtEmail, txtCoins, cartBadge;
    private ImageView settingIcon, cartIcon, profileImage;
    private LinearLayout layoutLogOut, layoutRating;
    private String userId;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth; // THÊM DÒNG NÀY
    private ListenerRegistration userListener, cartListener, orderListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        // Khởi tạo views
        initViews();

        // Lấy userId
        mAuth = FirebaseAuth.getInstance(); // THÊM DÒNG NÀY
        userId = mAuth.getCurrentUser().getUid();

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        // Load dữ liệu
        loadUserInfo();
        loadCartBadge();

        // Setup click listeners
        setupClickListeners();
    }

    private void initViews() {
        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        settingIcon = findViewById(R.id.settingIcon);
        cartIcon = findViewById(R.id.cartIcon);
        cartBadge = findViewById(R.id.cartBadge);
        profileImage = findViewById(R.id.profileImage);
        txtCoins = findViewById(R.id.txtCoins);
        layoutLogOut = findViewById(R.id.layoutLogOut); // THÊM DÒNG NÀY
        layoutRating = findViewById(R.id.layoutRating); // THÊM DÒNG NÀY
    }

    private void setupClickListeners() {
        // Click vào setting icon -> chuyển đến EditUserActivity
        settingIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MyAccountActivity.this, EditUserActivity.class);
            startActivity(intent);
        });

        // Click vào cart icon -> chuyển đến CartActivity
        cartIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MyAccountActivity.this, CartActivity.class);
            startActivity(intent);
        });

        // THÊM PHẦN NÀY - Click vào log out
        layoutLogOut.setOnClickListener(v -> {
            showLogoutDialog();
        });

        // THÊM CÁC CLICK LISTENERS CHO CÁC CHỨC NĂNG KHÁC
        findViewById(R.id.layoutNewOrder).setOnClickListener(v -> openOrdersByStatus("New Order"));
        findViewById(R.id.layoutProcessing).setOnClickListener(v -> openOrdersByStatus("Processing"));
        findViewById(R.id.layoutShipping).setOnClickListener(v -> openOrdersByStatus("Shipping"));
        findViewById(R.id.layoutCompleted).setOnClickListener(v -> openOrdersByStatus("Completed"));
        findViewById(R.id.layoutCancelled).setOnClickListener(v -> openOrdersByStatus("Cancelled"));

        findViewById(R.id.layoutSeen).setOnClickListener(v -> {
            // Mở màn hình sản phẩm đã xem
            Toast.makeText(this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.layoutFavorite).setOnClickListener(v -> {
            // Mở màn hình sản phẩm yêu thích
            Toast.makeText(this, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.layoutRating).setOnClickListener(v -> {
            // Mở màn hình feedback
            Toast.makeText(this, "Proceed to Feedback", Toast.LENGTH_SHORT).show();
            openFeedback();
        });
        findViewById(R.id.layoutCompleted).setOnClickListener(v -> {
            // Mở màn hình feedback
            Toast.makeText(this, "Proceed to Feedback", Toast.LENGTH_SHORT).show();
            openFeedback();
        });
    }

    private void openFeedback() {
        Intent intent = new Intent(this, DeliveredOrdersActivity.class);
        intent.putExtra("userId", userId);
        startActivity(intent);
    }

    // THÊM METHOD NÀY - Hiển thị dialog xác nhận logout
    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Log Out");
        builder.setMessage("Are you sure to log out?");
        builder.setIcon(R.mipmap.ic_logout);

        builder.setPositiveButton("Log Out", (dialog, which) -> {
            performLogout();
        });

        builder.setNegativeButton("Close", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // THÊM METHOD NÀY - Thực hiện logout
    private void performLogout() {
        try {
            // Hủy tất cả listeners trước khi logout
            removeAllListeners();

            // Đăng xuất khỏi Firebase Auth
            mAuth.signOut();

            // Chuyển về màn hình đăng nhập
            Intent intent = new Intent(MyAccountActivity.this, activity_login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

            // Hiển thị thông báo
            Toast.makeText(this, "Successful Log Out!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error Please Contact Calmpuchia: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // THÊM METHOD NÀY - Mở orders theo status
    private void openOrdersByStatus(String status) {
        Intent intent = new Intent(MyAccountActivity.this, OrdersActivity.class); // Thay vì activity_login
        intent.putExtra("status_filter", status);
        intent.putExtra("user_id", userId);
        startActivity(intent);
    }

    // THÊM METHOD NÀY - Hủy tất cả listeners
    private void removeAllListeners() {
        if (userListener != null) {
            userListener.remove();
            userListener = null;
        }
        if (cartListener != null) {
            cartListener.remove();
            cartListener = null;
        }
        if (orderListener != null) {
            orderListener.remove();
            orderListener = null;
        }
    }

    private void loadUserInfo() {
        userListener = db.collection("users")
                .document(userId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Toast.makeText(MyAccountActivity.this, "Không lấy được thông tin người dùng", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        // Lấy thông tin cơ bản
                        String name = documentSnapshot.getString("name");
                        String email = documentSnapshot.getString("email");
                        String imageUrl = documentSnapshot.getString("image");

                        // Lấy số loyalty_points (coins) - mặc định là 0 nếu chưa có
                        Long loyaltyPoints = documentSnapshot.getLong("loyalty_points");
                        if (loyaltyPoints == null) loyaltyPoints = 0L;

                        // Gán dữ liệu vào views
                        txtName.setText(name != null ? name : "");
                        txtEmail.setText(email != null ? email : "");
                        txtCoins.setText(loyaltyPoints + "");

                        // Load ảnh profile
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            if (imageUrl.startsWith("data:image")) {
                                // Xử lý Base64 image
                                Glide.with(MyAccountActivity.this)
                                        .load(imageUrl)
                                        .placeholder(R.mipmap.ic_customer)
                                        .error(R.mipmap.ic_customer)
                                        .into(profileImage);
                            } else {
                                // Xử lý URL image
                                Glide.with(MyAccountActivity.this)
                                        .load(imageUrl)
                                        .placeholder(R.mipmap.ic_customer)
                                        .error(R.mipmap.ic_customer)
                                        .into(profileImage);
                            }
                        } else {
                            profileImage.setImageResource(R.mipmap.ic_customer);
                        }
                    }
                });
    }

    private void loadCartBadge() {
        cartListener = db.collection("carts")
                .whereEqualTo("user_id", Long.parseLong(userId.replaceAll("[^0-9]", ""))) // Convert userId to number if needed
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        return;
                    }

                    int totalItems = 0;

                    if (queryDocumentSnapshots != null) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Lấy items array từ cart document
                            List<String> items = (List<String>) document.get("items");
                            if (items != null) {
                                totalItems += items.size();
                            }
                        }
                    }

                    // Hiển thị badge
                    if (totalItems > 0) {
                        cartBadge.setText(String.valueOf(totalItems));
                        cartBadge.setVisibility(TextView.VISIBLE);
                    } else {
                        cartBadge.setVisibility(TextView.GONE);
                    }
                });
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Refresh dữ liệu khi trở lại từ EditUserActivity
        loadUserInfo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy các listener để tránh memory leak
        removeAllListeners(); // SỬA LẠI METHOD NÀY
    }
}