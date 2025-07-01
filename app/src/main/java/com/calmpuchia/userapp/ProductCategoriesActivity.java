package com.calmpuchia.userapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.calmpuchia.userapp.adapters.CategoryAdapter;
import com.calmpuchia.userapp.models.Category;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProductCategoriesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CategoryAdapter categoryAdapter;
    private List<Category> categoryList;

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

        setupRecyclerView();
        loadCategories();
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(this, categoryList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(categoryAdapter);

        // Set click listener
        categoryAdapter.setOnCategoryClickListener(category -> {
            openProductList(category.getCategoryId());
        });
    }

    private void loadCategories() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("categories").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        categoryList.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            String iconUrl = document.getString("emoji");

                            Object rawCategoryId = document.get("category_id");
                            String categoryId = (rawCategoryId != null) ? rawCategoryId.toString() : "";

                            Category category = new Category(categoryId, name, iconUrl);
                            categoryList.add(category);
                        }

                        categoryAdapter.updateCategories(categoryList);
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