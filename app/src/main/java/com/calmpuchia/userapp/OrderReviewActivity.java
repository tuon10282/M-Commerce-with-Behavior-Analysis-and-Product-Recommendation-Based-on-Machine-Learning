package com.calmpuchia.userapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.calmpuchia.userapp.R;
import com.calmpuchia.userapp.adapters.OrderItemAdapter;
import com.calmpuchia.userapp.models.OrderItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrderReviewActivity extends AppCompatActivity {

    private static final String TAG = "OrderReviewActivity";
    private static final int REQUEST_CODE_REVIEW = 100;

    private ImageView btnBack;
    private TextView txtOrderId;
    private RecyclerView recyclerItems;
    private ProgressBar progressBar;
    private LinearLayout txtEmpty;

    private OrderItemAdapter adapter;
    private List<OrderItem> itemList;
    private FirebaseFirestore db;
    private String orderId;
    private String userId;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_review);

        initViews();
        initFirebase();
        getIntentData();
        setupRecyclerView();
        loadOrderItems();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        txtOrderId = findViewById(R.id.txt_order_id);
        recyclerItems = findViewById(R.id.recycler_items);
        progressBar = findViewById(R.id.progress_bar);
        txtEmpty = findViewById(R.id.txt_empty);

        btnBack.setOnClickListener(v -> finish());
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    private void getIntentData() {
        orderId = getIntent().getStringExtra("order_id");

        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (orderId != null) {
            txtOrderId.setText("Đơn hàng: " + orderId);
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupRecyclerView() {
        itemList = new ArrayList<>();
        adapter = new OrderItemAdapter(itemList, this::onItemClick);
        recyclerItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerItems.setAdapter(adapter);
    }

    private void loadOrderItems() {
        showLoading(true);

        db.collection("orders")
                .document(orderId)
                .collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    itemList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        OrderItem item = document.toObject(OrderItem.class);
                        if (item != null) {
                            Log.d(TAG, "Loaded item: " + item.getProduct_name() +
                                    ", ID: " + item.getProduct_id() +
                                    ", Reviewed: " + item.isReviewed());
                            itemList.add(item);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    showLoading(false);

                    if (itemList.isEmpty()) {
                        showEmptyState(true);
                    } else {
                        showEmptyState(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading order items", e);
                    Toast.makeText(this, "Có lỗi khi tải sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showLoading(false);
                    showEmptyState(true);
                });
    }

    private void onItemClick(OrderItem item) {
        if (item.isReviewed()) {
            Toast.makeText(this, "Bạn đã đánh giá sản phẩm này rồi!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra dữ liệu trước khi chuyển
        if (item.getProduct_id() == null || item.getProduct_name() == null) {
            Toast.makeText(this, "Thông tin sản phẩm không đầy đủ", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ProductReviewActivity.class);
        intent.putExtra("order_id", orderId);
        intent.putExtra("product_id", item.getProduct_id());
        intent.putExtra("product_name", item.getProduct_name());
        intent.putExtra("product_image", item.getProduct_image());
        intent.putExtra("user_id", userId);

        Log.d(TAG, "Starting review for product: " + item.getProduct_name() +
                ", ID: " + item.getProduct_id());

        startActivityForResult(intent, REQUEST_CODE_REVIEW);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_REVIEW && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("review_submitted", false)) {
                String reviewedProductId = data.getStringExtra("reviewed_product_id");

                // Cập nhật trạng thái reviewed cho item cụ thể
                if (reviewedProductId != null) {
                    updateItemReviewStatus(reviewedProductId);
                } else {
                    // Fallback: reload toàn bộ danh sách
                    loadOrderItems();
                }

                Toast.makeText(this, "Cảm ơn bạn đã đánh giá sản phẩm!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateItemReviewStatus(String productId) {
        // Cập nhật trạng thái trong Firebase
        db.collection("orders")
                .document(orderId)
                .collection("items")
                .whereEqualTo("product_id", productId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().update("reviewed", true)
                                .addOnSuccessListener(aVoid -> {
                                    // Cập nhật local list
                                    for (OrderItem item : itemList) {
                                        if (productId.equals(item.getProduct_id())) {
                                            item.setReviewed(true);
                                            break;
                                        }
                                    }
                                    adapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e ->
                                        Log.e(TAG, "Error updating review status", e));
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error finding item to update", e));
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerItems.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(boolean show) {
        txtEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerItems.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}