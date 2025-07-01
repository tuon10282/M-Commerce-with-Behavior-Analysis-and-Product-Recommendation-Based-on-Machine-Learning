package com.calmpuchia.userapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.calmpuchia.userapp.adapters.NotificationAdapter;
import com.calmpuchia.userapp.models.NotificationModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView recyclerViewNotifications;
    private NotificationAdapter notificationAdapter;
    private List<NotificationModel> notificationList;
    private LinearLayout layoutEmptyState;
    private TextView btnClearAll;
    private ImageButton btnBack;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userId = mAuth.getCurrentUser().getUid();

        initViews();
        setupRecyclerView();
        setupClickListeners();
        loadNotifications();
    }

    private void initViews() {
        recyclerViewNotifications = findViewById(R.id.recyclerViewNotifications);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        btnClearAll = findViewById(R.id.btnClearAll);
        btnBack = findViewById(R.id.btnBack);

        notificationList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        notificationAdapter = new NotificationAdapter(notificationList, this);
        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNotifications.setAdapter(notificationAdapter);

        notificationAdapter.setOnItemClickListener(notification -> {
            markAsRead(notification.getId());
            // Handle click action here
            Toast.makeText(this, "Clicked: " + notification.getTitle(), Toast.LENGTH_SHORT).show();
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnClearAll.setOnClickListener(v -> clearAllNotifications());
    }

    private void loadNotifications() {
        db.collection("notifications")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error loading notifications", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    notificationList.clear();
                    if (queryDocumentSnapshots != null) {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            NotificationModel notification = document.toObject(NotificationModel.class);
                            if (notification != null) {
                                notification.setId(document.getId());
                                notificationList.add(notification);
                            }
                        }
                    }

                    updateUI();
                });
    }

    private void updateUI() {
        if (notificationList.isEmpty()) {
            recyclerViewNotifications.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerViewNotifications.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
            notificationAdapter.notifyDataSetChanged();
        }
    }

    private void markAsRead(String notificationId) {
        db.collection("notifications")
                .document(notificationId)
                .update("read", true);
    }

    private void clearAllNotifications() {
        db.collection("notifications")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete();
                    }
                    Toast.makeText(this, "All notifications cleared", Toast.LENGTH_SHORT).show();
                });
    }

    // Method để tạo notification mẫu cho demo
    public void createSampleNotification() {
        Map<String, Object> notification = new HashMap<>();
        notification.put("userId", userId);
        notification.put("title", "Demo Notification");
        notification.put("message", "This is a sample notification for demo");
        notification.put("type", "general");
        notification.put("timestamp", System.currentTimeMillis());
        notification.put("read", false);

        db.collection("notifications")
                .add(notification)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(this, "Sample notification created", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error creating notification", Toast.LENGTH_SHORT).show());
    }
}