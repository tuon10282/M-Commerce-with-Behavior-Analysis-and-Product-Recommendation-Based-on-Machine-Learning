package com.calmpuchia.userapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.calmpuchia.userapp.adapters.ProductAdapter;
import com.calmpuchia.userapp.models.Products;
import com.google.firebase.firestore.*;

import java.util.*;
public class BestSellerActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Products> bestSellingProducts = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_best_seller);

        recyclerView = findViewById(R.id.recyclerViewBestSellers);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        adapter = new ProductAdapter(this, bestSellingProducts);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        fetchBestSellers();
    }

    private void fetchBestSellers() {
        db.collection("orders")
                .whereEqualTo("status", "completed")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Integer> productSalesMap = new HashMap<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        List<Map<String, Object>> items = (List<Map<String, Object>>) doc.get("items");
                        if (items != null) {
                            for (Map<String, Object> item : items) {
                                String productId = String.valueOf(item.get("product_id"));
                                int quantity = ((Number) item.get("quantity")).intValue();

                                productSalesMap.put(productId,
                                        productSalesMap.getOrDefault(productId, 0) + quantity);
                            }
                        }
                    }

                    // Sắp xếp sản phẩm theo số lượng bán ra (giảm dần)
                    List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(productSalesMap.entrySet());
                    sortedList.sort((a, b) -> b.getValue() - a.getValue());

                    // Lấy top 10 sản phẩm bán chạy
                    List<String> topProductIds = new ArrayList<>();
                    for (int i = 0; i < Math.min(10, sortedList.size()); i++) {
                        topProductIds.add(sortedList.get(i).getKey());
                    }

                    fetchProductDetails(topProductIds);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải đơn hàng", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error fetching orders", e);
                });
    }

    private void fetchProductDetails(List<String> topProductIds) {
        if (topProductIds.isEmpty()) return;

        db.collection("products")
                .whereIn("id", new ArrayList<>(topProductIds))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bestSellingProducts.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Products product = doc.toObject(Products.class);
                        bestSellingProducts.add(product);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải sản phẩm", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error fetching products", e);
                });
    }
}