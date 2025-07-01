package com.calmpuchia.userapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.calmpuchia.userapp.adapters.NearbyDealsAdapter;
import com.calmpuchia.userapp.adapters.ProductRecAdapter;
import com.calmpuchia.userapp.data.FirebaseRepository;
import com.calmpuchia.userapp.data.RecommendationRepository;
import com.calmpuchia.userapp.models.Products;
import com.calmpuchia.userapp.models.ProductsRec;
import com.calmpuchia.userapp.models.RecommendationResponse;
import com.calmpuchia.userapp.models.Store;
import com.calmpuchia.userapp.models.User;
import com.calmpuchia.userapp.utils.GeocodingHelper;
import com.calmpuchia.userapp.utils.LocationUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomepageActivity extends AppCompatActivity {
    private static final String TAG = "HomepageActivity";
    private RecyclerView recyclerView;
    private ProductRecAdapter adapter; // Đổi thành ProductRecAdapter
    private RecommendationRepository repository;
    private FirebaseRepository db;
    // Declare UI components
    private ImageView logo;
    private ImageView favoriteIcon;
    private ImageView notificationIcon;
    private ImageView cartIcon;
    private View navHome;
    private View navProduct;
    private View navCart;
    private View navAccount;
    private View navNotification;




    // Search functionality
    private EditText searchEditText;

    // Flash deal timer
    private TextView timerHours, timerMinutes, timerSeconds;
    private Handler timerHandler;
    private Runnable timerRunnable;
    private int totalSeconds = 7289; // 2h 1m 29s as shown in layout

    // Loading state
    private boolean isLoading = false;

//address recommendation
// UI components
    private RecyclerView addressRecyclerView;
    private NearbyDealsAdapter nearbyDealsAdapter;
    private TextView tvNearbyDealsTitle;

    // Data
    private List<Products> nearbyProducts = new ArrayList<>();

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private String currentUserId;
    private ListenerRegistration userListener;

    private RecyclerView bestSellersRecyclerView;
    private BestSellersAdapter bestSellersAdapter;
    private List<Products> bestSellingProducts = new ArrayList<>();
    private TextView tvSeeAllBestSellers;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        // CRITICAL: Initialize Firebase components FIRST
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        currentUserId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        View rootView = findViewById(R.id.root_view);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                return insets;
            });
        } else {
            Log.e("HomepageActivity", "rootView is null!");
        }

        addViews();
        addEvent();
        // Only call Firebase-dependent methods AFTER firestore is initialized
        if (firestore != null) {
            fetchBestSellers();
            initializeBestSellers();

            // Only load nearby products if user is authenticated
            if (currentUserId != null) {
                loadNearbyProducts();
            } else {
                Log.w(TAG, "User not authenticated, skipping nearby products");
            }
        } else {
            Log.e(TAG, "Firestore initialization failed");
            Toast.makeText(this, "Database connection failed", Toast.LENGTH_LONG).show();
        }
    }
    private void initializeBestSellers() {
        bestSellersRecyclerView = findViewById(R.id.popularrecyclerView);
        tvSeeAllBestSellers = findViewById(R.id.tvSeeAllBestSellers); // Cần thêm TextView này vào layout

        // Setup horizontal RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        bestSellersRecyclerView.setLayoutManager(layoutManager);

        bestSellersAdapter = new BestSellersAdapter(this, bestSellingProducts);
        bestSellersRecyclerView.setAdapter(bestSellersAdapter);

        // Click listener cho "See All"
        tvSeeAllBestSellers.setOnClickListener(v -> {
            Intent intent = new Intent(HomepageActivity.this, BestSellerActivity.class);
            startActivity(intent);
        });

        fetchBestSellersForHome();
    }

    private void addViews() {
        // Header components
        logo = findViewById(R.id.logo);
        favoriteIcon = findViewById(R.id.favoriteIcon);
        notificationIcon = findViewById(R.id.notificationIcon);
        cartIcon = findViewById(R.id.cartIcon);

        // Search functionality
        searchEditText = findViewById(R.id.searchEditText);

        // Navigation components
        navHome = findViewById(R.id.navHome);
        navProduct = findViewById(R.id.navProduct);
        navCart = findViewById(R.id.navCart);
        navAccount = findViewById(R.id.navAccount);
        navNotification = findViewById(R.id.navNotification);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        // Initialize empty adapter để tránh lỗi "No adapter attached"
        adapter = new ProductRecAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);

        repository = new RecommendationRepository();
        db = new FirebaseRepository();

        // Initialize nearby deals section
        addressRecyclerView = findViewById(R.id.addressrecyclerView);
        tvNearbyDealsTitle = findViewById(R.id.tvNearbyDealsTitle);

        // Setup RecyclerView for nearby deals
        nearbyDealsAdapter = new NearbyDealsAdapter(this, nearbyProducts);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        addressRecyclerView.setLayoutManager(layoutManager);
        addressRecyclerView.setAdapter(nearbyDealsAdapter);

        // Set click listener for title to navigate to NearbyProductsActivity
        if (tvNearbyDealsTitle != null) {
            tvNearbyDealsTitle.setOnClickListener(v -> {
                Intent intent = new Intent(HomepageActivity.this, NearbyProductsActivity.class);
                startActivity(intent);
            });

        }
    }


    private void loadNearbyProducts() {
        if (currentUserId == null) {
            Log.w(TAG, "Cannot load nearby products: User not authenticated");
            return;
        }

        Log.d(TAG, "Loading nearby products for user: " + currentUserId);

        // Get user address from Firestore
        userListener = firestore.collection("users").document(currentUserId)
                .addSnapshotListener((documentSnapshot, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Listen failed.", e);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            String address = user.getAddress();
                            if (address != null && !address.trim().isEmpty()) {
                                Log.d(TAG, "User address found: " + address);
                                convertAddressAndFindNearbyProducts(address.trim());
                            } else {
                                Log.w(TAG, "User address is empty");
                                // You might want to show a message to user to set their address
                            }
                        } else {
                            Log.w(TAG, "User object is null");
                        }
                    } else {
                        Log.w(TAG, "User data does not exist");
                    }
                });
    }

    private void convertAddressAndFindNearbyProducts(String address) {
        Log.d(TAG, "Converting address to coordinates: " + address);

        GeocodingHelper.getLatLngFromAddress(address, new GeocodingHelper.GeocodingCallback() {
            @Override
            public void onSuccess(double lat, double lng) {
                Log.d(TAG, "Address converted successfully: " + lat + ", " + lng);
                findNearestStoreAndLoadProducts(lat, lng);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error converting address to coordinates: " + error);
                // You might want to show an error message to the user
            }
        });
    }


private void findNearestStoreAndLoadProducts(double userLat, double userLng) {
    Log.d(TAG, "Finding nearest store to coordinates: " + userLat + ", " + userLng);

    firestore.collection("stores").get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                Store nearestStore = null;
                double minDistance = Double.MAX_VALUE;

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    try {
                        Store store = document.toObject(Store.class);
                        if (store != null && store.getLat() != 0 && store.getLng() != 0) {
                            double distance = LocationUtils.calculateDistance(
                                    userLat, userLng, store.getLat(), store.getLng()
                            );

                            Log.d(TAG, "Store: " + store.getStoreId() + ", Distance: " + distance + " km");

                            if (distance < minDistance) {
                                minDistance = distance;
                                nearestStore = store;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing store document: " + document.getId(), e);
                    }
                }

                if (nearestStore != null) {
                    Log.d(TAG, "Nearest store found: " + nearestStore.getStoreId() +
                            " at distance: " + minDistance + " km");
                    loadProductsFromStore(nearestStore.getStoreId());
                } else {
                    Log.w(TAG, "No nearest store found");
                    // You might want to show a message that no nearby stores are available
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading stores from Firestore", e);
            });
}

private void loadProductsFromStore(String storeId) {
    Log.d(TAG, "Loading products from store: " + storeId);

    firestore.collection("products")
            .whereEqualTo("store_id", storeId)
            .limit(5) // Only get first 5 products
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                nearbyProducts.clear();

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    try {
                        Products product = document.toObject(Products.class);
                        if (product != null) {
                            // Set document ID if product ID is null
                            if (product.getProduct_id() == null) {
                                product.setProduct_id(document.getId());
                            }
                            nearbyProducts.add(product);
                            Log.d(TAG, "Product added: " + product.getName());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing product document: " + document.getId(), e);
                    }
                }

                Log.d(TAG, "Total products loaded: " + nearbyProducts.size());

                // Update adapter on main thread
                runOnUiThread(() -> {
                    if (nearbyDealsAdapter != null) {
                        nearbyDealsAdapter.updateProducts(nearbyProducts);
                    }
                });
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading products from store: " + storeId, e);
            });
}


    private void fetchBestSellers() {
        db.getAllOrders()
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

                    List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(productSalesMap.entrySet());
                    sortedList.sort((a, b) -> b.getValue() - a.getValue());

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

        db.getAllProducts()
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

    private void addEvent() {
        
        loadRecommendations();
        logo.setOnClickListener(v -> {
            Toast.makeText(HomepageActivity.this, "Đã bấm ảnh ", Toast.LENGTH_SHORT).show();
            reload();
        });
        favoriteIcon.setOnClickListener(v -> {
            Toast.makeText(HomepageActivity.this, "Đã bấm ảnh ", Toast.LENGTH_SHORT).show();
            reload();
        });
        notificationIcon.setOnClickListener(v -> {
            Toast.makeText(HomepageActivity.this, "Đã bấm ảnh ", Toast.LENGTH_SHORT).show();
            reload();
        });
        cartIcon.setOnClickListener(v -> {
            Toast.makeText(HomepageActivity.this, "Đã bấm ảnh ", Toast.LENGTH_SHORT).show();
            openCart();
        });
        navHome.setOnClickListener(v -> {
            Toast.makeText(HomepageActivity.this, "Đã bấm ảnh ", Toast.LENGTH_SHORT).show();
            reload();
        });
        navProduct.setOnClickListener(v -> {
            Toast.makeText(HomepageActivity.this, "Đã bấm ảnh ", Toast.LENGTH_SHORT).show();
            openProductCategories();
        });
        navCart.setOnClickListener(v -> {
            Toast.makeText(HomepageActivity.this, "Đã bấm ảnh ", Toast.LENGTH_SHORT).show();
            reload();
        });
        navAccount.setOnClickListener(v -> {
            Toast.makeText(HomepageActivity.this, "Đã bấm ảnh ", Toast.LENGTH_SHORT).show();
            openAccount();
        });
        navNotification.setOnClickListener(v -> {
            Toast.makeText(HomepageActivity.this, "Đã bấm ảnh ", Toast.LENGTH_SHORT).show();
            openNotification();
        });
        // Set click listener for entire nearby deals section
        View nearbyDealsSection = findViewById(R.id.nearbyDealsSection);
        if (nearbyDealsSection != null) {
            nearbyDealsSection.setOnClickListener(v -> {
                Intent intent = new Intent(HomepageActivity.this, NearbyProductsActivity.class);
                startActivity(intent);
            });
        }



}

    private void openNotification() {
        Intent intent = new Intent(HomepageActivity.this, NotificationActivity.class);
        startActivity(intent);

    }

    private void openAccount() {
        Intent intent = new Intent(HomepageActivity.this, MyAccountActivity.class);
        startActivity(intent);
    }

    private void openProductCategories() {
        Intent intent = new Intent(HomepageActivity.this, ProductCategoriesActivity.class);
        startActivity(intent);

    }
    private void openCart() {
        Intent intent = new Intent(HomepageActivity.this, ProductCategoriesActivity.class);
        startActivity(intent);

    }


    private void loadRecommendations() {
        repository.getRecommendations("460189035", new Callback<RecommendationResponse>() {
            @Override
            public void onResponse(Call<RecommendationResponse> call, Response<RecommendationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RecommendationResponse recommendationResponse = response.body();
                    List<ProductsRec> productList = new ArrayList<>();

                    if (recommendationResponse.getRecommendations() != null) {
                        for (RecommendationResponse.Recommendation rec : recommendationResponse.getRecommendations()) {
                            if (rec.getProductInfo() != null) {
                                productList.add(rec.getProductInfo());
                            }
                        }
                    }

                    // Update adapter với ProductRec list
                    if (adapter == null) {
                        adapter = new ProductRecAdapter(HomepageActivity.this, productList);
                        recyclerView.setAdapter(adapter);
                    } else {
                        adapter.updateProductList(productList); // Nếu có method update
                    }

                    Log.d("API_RESPONSE", "Successfully loaded " + productList.size() + " products");
                    Log.d("API_RESPONSE", "Total recommendations: " + recommendationResponse.getCount());
                } else {
                    Log.e("API", "Response failed: " + response.code());
                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(Call<RecommendationResponse> call, Throwable t) {
                Log.e("API", "API call failed", t);
                showErrorMessage();
            }
        });
    }

    private void handleApiError(Response<RecommendationResponse> response) {
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                Log.e("API_ERROR", "Error response: " + errorBody);
            }
        } catch (Exception e) {
            Log.e("API_ERROR", "Cannot read error body", e);
        }
    }

    private void showErrorMessage() {
        // Hiển thị thông báo lỗi cho user
        // Có thể dùng Toast, Snackbar, hoặc dialog
    }
    private void reload()
    {
        Intent intent = new Intent(HomepageActivity.this, HomepageActivity.class);
        startActivity(intent);
    }

    private void fetchBestSellersForHome() {
        if (firestore == null) {
            Log.e(TAG, "Firestore is null in fetchBestSellersForHome");
            return;
        }

        firestore.collection("orders")
                .whereEqualTo("status", "delivered")
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

                    // Lấy top 10 sản phẩm bán chạy cho home page
                    List<String> topProductIds = new ArrayList<>();
                    for (int i = 0; i < Math.min(10, sortedList.size()); i++) {
                        topProductIds.add(sortedList.get(i).getKey());
                    }

                    fetchBestSellerProductDetails(topProductIds);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching best sellers for home", e);
                });
    }

    private void fetchBestSellerProductDetails(List<String> topProductIds) {
        if (topProductIds.isEmpty()) return;

        firestore.collection("products")
                .whereIn("id", new ArrayList<>(topProductIds))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bestSellingProducts.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Products product = doc.toObject(Products.class);
                        bestSellingProducts.add(product);
                    }
                    bestSellersAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching best seller products for home", e);
                });
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh nearby products when activity resumes
        if (currentUserId != null) {
            loadNearbyProducts();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up listener
        if (userListener != null) {
            userListener.remove();
        }
    }
}

