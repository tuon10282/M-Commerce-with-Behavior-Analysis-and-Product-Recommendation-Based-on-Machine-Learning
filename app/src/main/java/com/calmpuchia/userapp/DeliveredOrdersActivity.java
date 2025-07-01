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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.calmpuchia.userapp.adapters.DeliveredOrderAdapter;
import com.calmpuchia.userapp.models.Orders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DeliveredOrdersActivity extends AppCompatActivity {
    private static final String TAG = "DeliveredOrdersActivity";

    private ImageView btnBack;
    private RecyclerView recyclerOrders;
    private ProgressBar progressBar;
    private LinearLayout txtEmpty;

    private DeliveredOrderAdapter adapter;
    private List<Orders> orderList;
    private FirebaseFirestore db;
    private String userId;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_delivered_orders);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        initFirebase();
        getUserId();
        setupRecyclerView();
        loadDeliveredOrders();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        recyclerOrders = findViewById(R.id.recycler_orders);
        progressBar = findViewById(R.id.progress_bar);
        txtEmpty = findViewById(R.id.txt_empty);

        btnBack.setOnClickListener(v -> finish());
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
    }

    private void getUserId() {
        // Get userId from SharedPreferences or from Intent
        userId = getIntent().getStringExtra("user_id");
        if (userId == null) {
            mAuth = FirebaseAuth.getInstance();
            if (mAuth.getCurrentUser() != null) {
                userId = mAuth.getCurrentUser().getUid();
            }
        }
    }

    private void setupRecyclerView() {
        orderList = new ArrayList<>();
        adapter = new DeliveredOrderAdapter(orderList, order -> {
            // Comment out OrderReviewActivity until it's created
             Intent intent = new Intent(this, OrderReviewActivity.class);
             intent.putExtra("order_id", order.getOrder_id());
             intent.putExtra("user_id", userId);
             startActivity(intent);

            // Temporary toast to show order click works
            Toast.makeText(this, "Clicked order: " + order.getOrder_id(), Toast.LENGTH_SHORT).show();
        });
        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerOrders.setAdapter(adapter);
    }

    private void loadDeliveredOrders() {
        showLoading(true);

        db.collection("orders")
                .whereEqualTo("user_id", userId)
                .whereEqualTo("status", "delivered")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Orders order = document.toObject(Orders.class);
                        order.setOrder_id(document.getId());
                        orderList.add(order);
                    }

                    // Chỉ cần notify adapter
                    adapter.notifyDataSetChanged();

                    showLoading(false);

                    if (orderList.isEmpty()) {
                        showEmptyState(true);
                    } else {
                        showEmptyState(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading delivered orders", e);
                    Toast.makeText(this, "Có lỗi khi tải danh sách đơn hàng", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
    }



    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerOrders.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(boolean show) {
        txtEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerOrders.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}