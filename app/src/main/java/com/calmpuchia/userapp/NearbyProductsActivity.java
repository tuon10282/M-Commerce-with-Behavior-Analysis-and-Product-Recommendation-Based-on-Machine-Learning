package com.calmpuchia.userapp;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.calmpuchia.userapp.adapters.ProductCardAdapter;
import com.calmpuchia.userapp.models.Products;
import com.calmpuchia.userapp.models.Store;
import com.calmpuchia.userapp.models.User;
import com.calmpuchia.userapp.utils.GeocodingHelper;
import com.calmpuchia.userapp.utils.LocationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class NearbyProductsActivity extends AppCompatActivity {

    private static final String TAG = "NearbyProducts";

    private RecyclerView recyclerViewProducts;
    private ProductCardAdapter productAdapter;
    private ProgressBar progressBar;
    private TextView tvNoProducts, tvLocationStatus;
    private MaterialButton btnUpdateLocation;
    private View layoutNoAddress;
    private LinearLayout layoutOtherStores;
    private ImageView btnBack;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private String currentUserId;
    private ListenerRegistration userListener;

    private List<Products> allProducts = new ArrayList<>();
    private List<StoreWithDistance> allStoresWithDistance = new ArrayList<>();
    private Store nearestStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_products);

        Log.d(TAG, "=== STARTING NearbyProductsActivity ===");

        initFirebase();
        initViews();
        setupRecyclerView();

        // Debug methods để kiểm tra dữ liệu
/*        debugAllStores();
        debugAllProducts();*/

        checkUserLocationAndLoadProducts();
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
            Log.d(TAG, "Current user ID: " + currentUserId);
        } else {
            Log.e(TAG, "No current user found!");
        }
        firestore = FirebaseFirestore.getInstance();
    }

    private void initViews() {
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        progressBar = findViewById(R.id.progressBar);
        tvNoProducts = findViewById(R.id.tvNoProducts);
        tvLocationStatus = findViewById(R.id.tvLocationStatus);
        btnUpdateLocation = findViewById(R.id.btnUpdateLocation);
        layoutNoAddress = findViewById(R.id.layoutNoAddress);
        layoutOtherStores = findViewById(R.id.layoutOtherStores);
        btnBack = findViewById(R.id.btnBack);

        btnUpdateLocation.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditUserActivity.class);
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> {
            onBackPressed();
        });
    }

    private void setupRecyclerView() {
        productAdapter = new ProductCardAdapter(this, allProducts);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerViewProducts.setLayoutManager(gridLayoutManager);
        recyclerViewProducts.setAdapter(productAdapter);
        recyclerViewProducts.setHasFixedSize(true);
        recyclerViewProducts.setNestedScrollingEnabled(false);
    }

    private void checkUserLocationAndLoadProducts() {
        if (currentUserId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading();
        Log.d(TAG, "Checking user location...");

        // Sử dụng Firestore thay vì Realtime Database
        userListener = firestore.collection("users").document(currentUserId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Listen failed.", e);
                        hideLoading();
                        Toast.makeText(NearbyProductsActivity.this, "Lỗi khi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null && user.getAddress() != null && !user.getAddress().trim().isEmpty()) {
                            Log.d(TAG, "User address found: " + user.getAddress());
                            hideNoAddressLayout();
                            convertAddressToLatLngAndFindProducts(user.getAddress().trim());
                        } else {
                            Log.d(TAG, "User has no address");
                            hideLoading();
                            showNoAddressLayout();
                        }
                    } else {
                        Log.e(TAG, "User data not found in database");
                        hideLoading();
                        showNoAddressLayout();
                    }
                });
    }

    private void convertAddressToLatLngAndFindProducts(String address) {
        tvLocationStatus.setText("Đang xác định vị trí của bạn...");
        Log.d(TAG, "Converting address to coordinates: " + address);

        GeocodingHelper.getLatLngFromAddress(address, new GeocodingHelper.GeocodingCallback() {
            @Override
            public void onSuccess(double lat, double lng) {
                Log.d(TAG, "Geocoding success - Lat: " + lat + ", Lng: " + lng);
                tvLocationStatus.setText("Đã xác định vị trí: " + address);
                findNearestStoreAndProducts(lat, lng);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Geocoding error: " + error);
                hideLoading();
                tvLocationStatus.setText("Không thể xác định vị trí từ địa chỉ");
                Toast.makeText(NearbyProductsActivity.this, "Lỗi geocoding: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void findNearestStoreAndProducts(double userLat, double userLng) {
        tvLocationStatus.setText("Đang tìm cửa hàng gần nhất...");
        Log.d(TAG, "Finding nearest store from user location: " + userLat + ", " + userLng);

        firestore.collection("stores").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " stores in database");

                    allStoresWithDistance.clear();
                    nearestStore = null;
                    double minDistance = Double.MAX_VALUE;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Store store = document.toObject(Store.class);
                        if (store != null && store.getLat() != 0 && store.getLng() != 0) {
                            double distance = LocationUtils.calculateDistance(userLat, userLng, store.getLat(), store.getLng());

                            Log.d(TAG, "Store: " + store.getAddress() +
                                    " | Store ID: " + store.getStoreId() +
                                    " | Distance: " + String.format("%.2f", distance) + "km");

                            StoreWithDistance storeWithDistance = new StoreWithDistance(store, distance);
                            allStoresWithDistance.add(storeWithDistance);

                            if (distance < minDistance) {
                                minDistance = distance;
                                nearestStore = store;
                            }
                        } else {
                            Log.w(TAG, "Store has invalid coordinates: " + document.getId());
                        }
                    }

                    if (nearestStore == null) {
                        Log.e(TAG, "No valid store found");
                        hideLoading();
                        tvLocationStatus.setText("Không tìm thấy cửa hàng nào");
                        showNoProducts();
                    } else {
                        Log.d(TAG, "Nearest store selected: " + nearestStore.getAddress() +
                                " | Store ID: " + nearestStore.getStoreId() +
                                " | Distance: " + String.format("%.1f", minDistance) + "km");

                        tvLocationStatus.setText("Cửa hàng gần nhất: " + nearestStore.getAddress() +
                                " (cách " + String.format("%.1f", minDistance) + "km)");
                        loadProductsFromNearestStore();
                        displayOtherStores();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading stores: " + e.getMessage());
                    hideLoading();
                    tvLocationStatus.setText("Lỗi khi tải danh sách cửa hàng");
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadProductsFromNearestStore() {
        Log.d(TAG, "=== LOADING PRODUCTS FROM NEAREST STORE ===");

        if (nearestStore == null) {
            Log.e(TAG, "Nearest store is null!");
            hideLoading();
            showNoProducts();
            return;
        }

        Log.d(TAG, "Nearest store ID: " + nearestStore.getStoreId());
        Log.d(TAG, "Nearest store address: " + nearestStore.getAddress());
        Log.d(TAG, "Querying products with store_id = " + nearestStore.getStoreId());

        showLoading(); // Đảm bảo hiện loading trước khi gọi Firestore

        firestore.collection("products")
                .whereEqualTo("store_id", nearestStore.getStoreId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allProducts.clear(); // Xóa danh sách hiện tại trước khi thêm mới

                    Log.d(TAG, "Products query returned " + queryDocumentSnapshots.size() + " documents");

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Products product = document.toObject(Products.class);
                        if (product != null) {
                            if (product.getProduct_id() == null) {
                                product.setProduct_id(document.getId());
                            }
                            allProducts.add(product);
                            Log.d(TAG, "Added product: " + product.getName());
                        } else {
                            Log.e(TAG, "Could not parse product: " + document.getId());
                        }
                    }

                    hideLoading();
                    Log.d(TAG, "Final products count: " + allProducts.size());

                    if (allProducts.isEmpty()) {
                        showNoProducts();
                    } else {
                        showProducts();
                        productAdapter.notifyDataSetChanged(); // Sử dụng notifyDataSetChanged() cho ProductCardAdapter
                        Log.d(TAG, "Adapter updated with " + allProducts.size() + " products");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading products: " + e.getMessage(), e);
                    hideLoading();
                    Toast.makeText(this, "Lỗi khi tải sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void displayOtherStores() {
        Log.d(TAG, "Displaying other stores");
        layoutOtherStores.removeAllViews();

        // Thêm header cho danh sách cửa hàng khác
        TextView headerView = new TextView(this);
        headerView.setText("Các cửa hàng khác:");
        headerView.setTextSize(16);
        headerView.setTextColor(getResources().getColor(R.color.text_primary));
        headerView.setPadding(16, 16, 16, 8);
        headerView.setTypeface(headerView.getTypeface(), Typeface.BOLD);
        layoutOtherStores.addView(headerView);

        for (StoreWithDistance storeWithDistance : allStoresWithDistance) {
            if (!storeWithDistance.store.getStoreId().equals(nearestStore.getStoreId())) {

                // Tạo card view cho mỗi cửa hàng
                CardView cardView = new CardView(this);
                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                cardParams.setMargins(16, 8, 16, 8);
                cardView.setLayoutParams(cardParams);
                cardView.setCardElevation(4);
                cardView.setRadius(12);
                cardView.setUseCompatPadding(true);

                // Layout bên trong card
                LinearLayout innerLayout = new LinearLayout(this);
                innerLayout.setOrientation(LinearLayout.VERTICAL);
                innerLayout.setPadding(16, 12, 16, 12);

                // Tên/địa chỉ cửa hàng
                TextView tvStoreName = new TextView(this);
                tvStoreName.setText(storeWithDistance.store.getAddress());
                tvStoreName.setTextSize(14);
                tvStoreName.setTextColor(getResources().getColor(R.color.text_primary));
                tvStoreName.setTypeface(tvStoreName.getTypeface(), Typeface.BOLD);
                tvStoreName.setMaxLines(2);
                tvStoreName.setEllipsize(TextUtils.TruncateAt.END);

                // Khoảng cách
                TextView tvDistance = new TextView(this);
                tvDistance.setText("Cách bạn " + String.format("%.1f", storeWithDistance.distance) + "km");
                tvDistance.setTextSize(12);
                tvDistance.setTextColor(getResources().getColor(R.color.text_secondary));

                innerLayout.addView(tvStoreName);
                innerLayout.addView(tvDistance);
                cardView.addView(innerLayout);

                // Click listener để xem sản phẩm của cửa hàng này
                cardView.setOnClickListener(v -> {
                    loadProductsFromSpecificStore(storeWithDistance.store);
                });

                layoutOtherStores.addView(cardView);
            }
        }
    }

    private void loadProductsFromSpecificStore(Store store) {
        Log.d(TAG, "Loading products from specific store: " + store.getAddress());
        showLoading();
        allProducts.clear();

        // Cập nhật thông tin cửa hàng hiện tại
        nearestStore = store;
        tvLocationStatus.setText("Đang tải sản phẩm từ: " + store.getAddress());

        firestore.collection("products")
                .whereEqualTo("store_id", store.getStoreId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " products for store: " + store.getAddress());

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Products product = document.toObject(Products.class);
                        if (product != null) {
                            if (product.getProduct_id() == null) {
                                product.setProduct_id(document.getId());
                            }
                            allProducts.add(product);
                        }
                    }

                    hideLoading();
                    tvLocationStatus.setText("Sản phẩm từ: " + store.getAddress());

                    if (allProducts.isEmpty()) {
                        showNoProducts();
                    } else {
                        showProducts();
                        // Cập nhật adapter với danh sách sản phẩm mới
                        productAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading products for specific store: " + e.getMessage());
                    hideLoading();
                    Toast.makeText(this, "Lỗi khi tải sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Method debug để kiểm tra tất cả products trong Firestore
/*    private void debugAllProducts() {
        Log.d(TAG, "=== DEBUG: CHECKING ALL PRODUCTS IN FIRESTORE ===");

        firestore.collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Total products in Firestore: " + queryDocumentSnapshots.size());

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Log.d(TAG, "Product ID: " + document.getId());
                        Log.d(TAG, "  - Store ID: " + document.getString("store_id"));
                        Log.d(TAG, "  - Product name: " + document.getString("name"));
                        Log.d(TAG, "  - Price: " + document.get("price"));
                        Log.d(TAG, "---");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting all products: " + e.getMessage());
                });
    }*/

    // Method debug để kiểm tra tất cả stores
   /* private void debugAllStores() {
        Log.d(TAG, "=== DEBUG: CHECKING ALL STORES IN FIRESTORE ===");

        firestore.collection("stores")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Total stores in Firestore: " + queryDocumentSnapshots.size());

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Log.d(TAG, "Store Document ID: " + document.getId());
                        Log.d(TAG, "  - Store store_id field: " + document.getString("store_id"));
                        Log.d(TAG, "  - Store storeId field: " + document.getString("storeId"));
                        Log.d(TAG, "  - Store address: " + document.getString("address"));
                        Log.d(TAG, "  - Store lat: " + document.getDouble("lat"));
                        Log.d(TAG, "  - Store lng: " + document.getDouble("lng"));
                        Log.d(TAG, "---");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting all stores: " + e.getMessage());
                });
    }*/

    private void showLoading() {
        Log.d(TAG, "Showing loading...");
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewProducts.setVisibility(View.GONE);
        tvNoProducts.setVisibility(View.GONE);
        layoutNoAddress.setVisibility(View.GONE);
        layoutOtherStores.setVisibility(View.GONE);
    }

    private void hideLoading() {
        Log.d(TAG, "Hiding loading...");
        progressBar.setVisibility(View.GONE);
    }

    private void showProducts() {
        Log.d(TAG, "Showing products view");
        recyclerViewProducts.setVisibility(View.VISIBLE);
        tvNoProducts.setVisibility(View.GONE);
        layoutNoAddress.setVisibility(View.GONE);
        layoutOtherStores.setVisibility(View.VISIBLE);
    }

    private void showNoProducts() {
        Log.d(TAG, "Showing no products view");
        recyclerViewProducts.setVisibility(View.GONE);
        tvNoProducts.setVisibility(View.VISIBLE);
        tvNoProducts.setText("Cửa hàng này chưa có sản phẩm nào.");
        layoutNoAddress.setVisibility(View.GONE);
        layoutOtherStores.setVisibility(View.VISIBLE);
    }

    private void showNoAddressLayout() {
        Log.d(TAG, "Showing no address layout");
        recyclerViewProducts.setVisibility(View.GONE);
        tvNoProducts.setVisibility(View.GONE);
        layoutNoAddress.setVisibility(View.VISIBLE);
        layoutOtherStores.setVisibility(View.GONE);
    }

    private void hideNoAddressLayout() {
        Log.d(TAG, "Hiding no address layout");
        layoutNoAddress.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        // Chỉ reload nếu chưa có dữ liệu
        if (allProducts.isEmpty()) {
            Log.d(TAG, "No products loaded, reloading...");
            checkUserLocationAndLoadProducts();
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "Back pressed");
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up listener
        if (userListener != null) {
            userListener.remove();
        }
    }

    // Inner class để lưu store với khoảng cách
    private static class StoreWithDistance {
        Store store;
        double distance;

        StoreWithDistance(Store store, double distance) {
            this.store = store;
            this.distance = distance;
        }
    }
}