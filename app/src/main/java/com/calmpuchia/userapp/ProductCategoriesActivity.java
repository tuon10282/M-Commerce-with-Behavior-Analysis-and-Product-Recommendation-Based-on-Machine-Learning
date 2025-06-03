package com.calmpuchia.userapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ProductCategoriesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_categories);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        loadCategories();
    }

    private void loadCategories() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("categories").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        LinearLayout layout = findViewById(R.id.categoryLayout);
                        layout.removeAllViews();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");

                            // Ở đây "emoji" là link ảnh, ví dụ: https://...jpg
                            String iconUrl = document.getString("emoji");

                            Object rawCategoryId = document.get("category_id");
                            String categoryId = (rawCategoryId != null) ? rawCategoryId.toString() : "";

                            // Tạo layout con cho từng item danh mục
                            LinearLayout itemLayout = new LinearLayout(this);
                            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                            itemLayout.setPadding(24, 20, 24, 20);
                            itemLayout.setBackgroundResource(android.R.drawable.list_selector_background);
                            itemLayout.setClickable(true);
                            itemLayout.setOnClickListener(v -> openProductList(categoryId));

                            // ImageView cho icon ảnh
                            ImageView iconView = new ImageView(this);
                            int iconSize = (int) (48 * getResources().getDisplayMetrics().density); // 48dp
                            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(iconSize, iconSize);
                            iconParams.setMargins(0, 0, 24, 0); // margin phải cho cách text
                            iconView.setLayoutParams(iconParams);

                            if (iconUrl != null && !iconUrl.isEmpty()) {
                                Glide.with(this)
                                        .load(iconUrl)
                                        .centerCrop()
                                        .into(iconView);
                            } else {
                                // Nếu không có icon, có thể đặt icon mặc định hoặc ẩn
                                iconView.setImageResource(android.R.drawable.ic_menu_gallery);
                            }

                            // TextView cho tên category
                            TextView tv = new TextView(this);
                            tv.setText(name != null ? name : "");
                            tv.setTextSize(16);
                            tv.setLayoutParams(new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT));

                            // Thêm vào layout item: icon + tên
                            itemLayout.addView(iconView);
                            itemLayout.addView(tv);

                            // Thêm itemLayout vào layout chính
                            layout.addView(itemLayout);
                        }
                    } else {
                        Log.e("Firestore", "Lỗi tải danh mục", task.getException());
                        Toast.makeText(this, "Không thể tải danh mục", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openProductList(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy danh mục!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(ProductCategoriesActivity.this, ProductListActivity.class);
        intent.putExtra("category_id", categoryId);
        startActivity(intent);
    }
}
