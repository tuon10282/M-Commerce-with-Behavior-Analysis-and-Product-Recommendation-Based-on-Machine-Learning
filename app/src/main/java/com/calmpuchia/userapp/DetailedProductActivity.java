package com.calmpuchia.userapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.calmpuchia.userapp.adapters.HorizontalProductAdapter;
import com.calmpuchia.userapp.models.Products;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailedProductActivity extends AppCompatActivity {
    private Button addToCartButton;
    private ImageView productImage;
    private ImageButton btnIncrease, btnDecrease, btnWishlist;
    private TextView productPrice, productDescription, productName, numberText, tvSeeAll;
    private RecyclerView recommendationRecyclerView;
    private HorizontalProductAdapter recommendationAdapter;
    private FloatingActionButton fabShare;

    private String productID = "";
    private int quantity = 1;
    private double unitPrice = 0.0;
    private double discountPrice = 0.0;
    private boolean hasDiscount = false;
    private String name = "";
    private String image = "";
    private ArrayList<String> descriptionList;
    private ArrayList<String> currentProductTags;
    private boolean isWishlisted = false;

    private FirebaseFirestore db;
    private List<Products> recommendedProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detailed_product);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        recommendedProducts = new ArrayList<>();

        addViews();
        bindData();
        addEvents();

        // Lấy thông tin đầy đủ của sản phẩm từ Firestore để có tags
        getCurrentProductDetails();
    }

    private void addViews() {
        addToCartButton = findViewById(R.id.pd_add_to_cart_button);
        productImage = findViewById(R.id.product_image_details);
        productName = findViewById(R.id.product_name_details);
        productDescription = findViewById(R.id.product_description_details);
        productPrice = findViewById(R.id.product_price_details);
        numberText = findViewById(R.id.number_text);
        btnIncrease = findViewById(R.id.btn_increase);
        btnDecrease = findViewById(R.id.btn_decrease);
        btnWishlist = findViewById(R.id.btn_wishlist);
        tvSeeAll = findViewById(R.id.tv_see_all);
        fabShare = findViewById(R.id.fab_share);
        recommendationRecyclerView = findViewById(R.id.recommendation_recycler_view);

        // Setup RecyclerView cho sản phẩm đề xuất
        recommendationRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recommendationAdapter = new HorizontalProductAdapter(this, recommendedProducts);
        recommendationRecyclerView.setAdapter(recommendationAdapter);

        // Lấy dữ liệu từ Intent
        productID = getIntent().getStringExtra("pid");
        name = getIntent().getStringExtra("name");
        descriptionList = getIntent().getStringArrayListExtra("description");
        image = getIntent().getStringExtra("image");
        unitPrice = getIntent().getDoubleExtra("price", 0.0);

        if (productID == null || productID.isEmpty()) {
            Toast.makeText(this, "Không thể hiển thị sản phẩm: ID bị thiếu.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void bindData() {
        productName.setText(name);
        productPrice.setText(String.format("%.0f đ", unitPrice));
        numberText.setText(String.valueOf(quantity));

        if (descriptionList != null && !descriptionList.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (String line : descriptionList) {
                builder.append("• ").append(line).append("\n");
            }
            productDescription.setText(builder.toString());
        } else {
            productDescription.setText("Không có mô tả.");
        }

        // Hiển thị hình ảnh bằng Glide với placeholder và error handling
        Glide.with(this)
                .load(image)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(productImage);

        // Cập nhật button text ban đầu
        updateAddToCartButton();
    }

    private void addEvents() {
        btnIncrease.setOnClickListener(v -> {
            quantity++;
            numberText.setText(String.valueOf(quantity));
            updateAddToCartButton();
        });

        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                numberText.setText(String.valueOf(quantity));
                updateAddToCartButton();
            }
        });

        addToCartButton.setOnClickListener(v -> {
            // Animate button press
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                    .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(100));

            // Gọi hàm thêm vào giỏ hàng
            addingToCartList();
        });

        // Xử lý nút back
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            // Add back animation
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100)
                    .withEndAction(() -> {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100);
                        finish();
                    });
        });

        // Xử lý nút cart (có thể mở CartActivity)
        findViewById(R.id.btnMoreOptions).setOnClickListener(v -> {
            // TODO: Mở CartActivity hoặc hiển thị cart
            startActivity(new Intent(this, CartActivity.class));
        });

        // Xử lý nút wishlist
        btnWishlist.setOnClickListener(v -> {
            isWishlisted = !isWishlisted;
            updateWishlistButton();
            String message = isWishlisted ? "Đã thêm vào danh sách yêu thích" : "Đã xóa khỏi danh sách yêu thích";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            // TODO: Cập nhật wishlist trong database
        });

        // Xử lý nút "Xem tất cả" sản phẩm liên quan
        tvSeeAll.setOnClickListener(v -> {
            // TODO: Mở activity hiển thị tất cả sản phẩm liên quan
            Toast.makeText(this, "Xem tất cả sản phẩm liên quan", Toast.LENGTH_SHORT).show();
        });

        // Xử lý nút share
        fabShare.setOnClickListener(v -> {
            shareProduct();
        });
    }

    private void updateAddToCartButton() {
        // Update button text with quantity
        addToCartButton.setText("ADD " + quantity + " TO CART");
    }
    public void onProductViewed(String productId) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập trước khi thêm vào giỏ hàng.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, activity_login.class));
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        // Track user behavior for smart notifications
        Map<String, Object> interactions = new HashMap<>();
        interactions.put("userId", userId);
        interactions.put("productId", productId);
        interactions.put("action", "view");
        interactions.put("timestamp", System.currentTimeMillis());

        db.collection("interactions").add(interactions);

    }


    private void addingToCartList() {
        // Kiểm tra đăng nhập
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập trước khi thêm vào giỏ hàng.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, activity_login.class));
            return;
        }

        String userId = auth.getCurrentUser().getUid();

        // Tính giá hiệu quả và tổng giá
        double effectivePrice = hasDiscount ? discountPrice : unitPrice;
        double totalPrice = effectivePrice * quantity;

        // Tạo Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Tạo map cho items (sản phẩm trong giỏ hàng)
        HashMap<String, Object> itemMap = new HashMap<>();
        itemMap.put("product_id", productID);
        itemMap.put("name", name);
        itemMap.put("price", effectivePrice);
        itemMap.put("quantity", quantity);
        itemMap.put("total_price", totalPrice);
        itemMap.put("image_url", image);
        itemMap.put("discount_price", hasDiscount ? discountPrice : 0.0);
        itemMap.put("has_discount", hasDiscount);

        // Tạo cart data
        HashMap<String, Object> cartData = new HashMap<>();
        cartData.put("cart_id", System.currentTimeMillis()); // Sử dụng timestamp làm cart_id
        cartData.put("user_id", userId);
        cartData.put("updated_at", new com.google.firebase.Timestamp(new java.util.Date()));

        // Tạo items map với product_id làm key
        HashMap<String, Object> itemsMap = new HashMap<>();
        itemsMap.put(productID, itemMap);
        cartData.put("items", itemsMap);

        // Kiểm tra xem user đã có cart chưa
        db.collection("carts")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // User đã có cart, update cart hiện tại
                        String cartDocumentId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Update hoặc thêm item mới vào cart
                        String itemPath = "items." + productID;
                        HashMap<String, Object> updateData = new HashMap<>();
                        updateData.put(itemPath, itemMap);
                        updateData.put("updated_at", new com.google.firebase.Timestamp(new java.util.Date()));

                        db.collection("carts").document(cartDocumentId)
                                .update(updateData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(DetailedProductActivity.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(DetailedProductActivity.this, CartActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(DetailedProductActivity.this, "Lỗi khi thêm vào giỏ hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // User chưa có cart, tạo cart mới
                        db.collection("carts")
                                .add(cartData)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(DetailedProductActivity.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(DetailedProductActivity.this, CartActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(DetailedProductActivity.this, "Lỗi khi tạo giỏ hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi kiểm tra giỏ hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateWishlistButton() {
        // Update wishlist button appearance
        if (isWishlisted) {
            btnWishlist.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            btnWishlist.setImageResource(android.R.drawable.btn_star_big_off);
        }
    }

    private void updatePriceDisplay() {
        if (hasDiscount) {
            // Hiển thị giá discount
            productPrice.setText(String.format("%.0f $", discountPrice));
            // Có thể thêm logic hiển thị giá gốc bị gạch ngang nếu có TextView riêng
        } else {
            productPrice.setText(String.format("%.0f $", unitPrice));
        }
    }

    private void shareProduct() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String shareText = "Hãy xem sản phẩm này: " + name + "\nGiá: " + String.format("%.0f $", unitPrice);
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Chia sẻ sản phẩm");

        try {
            startActivity(Intent.createChooser(shareIntent, "Chia sẻ sản phẩm qua"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Không thể chia sẻ sản phẩm", Toast.LENGTH_SHORT).show();
        }
    }

    // Lấy thông tin đầy đủ của sản phẩm hiện tại từ Firestore
    private void getCurrentProductDetails() {
        db.collection("products")
                .document(productID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Products currentProduct = documentSnapshot.toObject(Products.class);
                        if (currentProduct != null) {
                            // Since discount_price is primitive double, it can't be null
                            // Check if it's greater than 0 instead
                            if (currentProduct.getDiscount_price() > 0) {
                                discountPrice = currentProduct.getDiscount_price();
                                hasDiscount = true;
                                // Cập nhật UI hiển thị giá
                                updatePriceDisplay();
                            }

                            // Lấy tags để tìm sản phẩm đề xuất
                            if (currentProduct.getTags() != null) {
                                currentProductTags = new ArrayList<>(currentProduct.getTags());
                                getRecommendedProducts();
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DetailedProduct", "Error getting current product: ", e);
                });
    }
    // Tìm sản phẩm đề xuất dựa trên tags
    private void getRecommendedProducts() {
        if (currentProductTags == null || currentProductTags.isEmpty()) {
            return;
        }

        // Query tất cả sản phẩm có chứa ít nhất 1 tag giống với sản phẩm hiện tại
        db.collection("products")
                .whereArrayContainsAny("tags", currentProductTags)
                .limit(10) // Giới hạn số lượng kết quả
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    recommendedProducts.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Products product = document.toObject(Products.class);

                        // Loại bỏ sản phẩm hiện tại khỏi danh sách đề xuất
                        if (!product.getProduct_id().equals(productID)) {
                            // Tính điểm tương đồng dựa trên số tags chung
                            int similarityScore = calculateSimilarityScore(product.getTags());

                            // Chỉ thêm sản phẩm có điểm tương đồng > 0
                            if (similarityScore > 0) {
                                recommendedProducts.add(product);
                            }
                        }
                    }

                    // Sắp xếp theo điểm tương đồng (giảm dần)
                    recommendedProducts.sort((p1, p2) -> {
                        int score1 = calculateSimilarityScore(p1.getTags());
                        int score2 = calculateSimilarityScore(p2.getTags());
                        return Integer.compare(score2, score1);
                    });

                    // Giới hạn 5 sản phẩm đề xuất hàng đầu
                    if (recommendedProducts.size() > 5) {
                        recommendedProducts = recommendedProducts.subList(0, 5);
                    }

                    // Cập nhật adapter
                    recommendationAdapter.notifyDataSetChanged();

                    Log.d("DetailedProduct", "Found " + recommendedProducts.size() + " recommended products");
                })
                .addOnFailureListener(e -> {
                    Log.e("DetailedProduct", "Error getting recommended products: ", e);
                });
    }

    // Tính điểm tương đồng dựa trên số tags chung
    private int calculateSimilarityScore(List<String> productTags) {
        if (productTags == null || currentProductTags == null) {
            return 0;
        }

        int commonTags = 0;
        for (String tag : productTags) {
            if (currentProductTags.contains(tag)) {
                commonTags++;
            }
        }
        return commonTags;
    }

    // Phương thức alternative: Query theo từng tag riêng lẻ (nếu whereArrayContainsAny không hoạt động)
    private void getRecommendedProductsAlternative() {
        if (currentProductTags == null || currentProductTags.isEmpty()) {
            return;
        }

        recommendedProducts.clear();

        // Duyệt qua từng tag và tìm sản phẩm
        for (String tag : currentProductTags) {
            db.collection("products")
                    .whereArrayContains("tags", tag)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Products product = document.toObject(Products.class);

                            // Kiểm tra xem sản phẩm đã có trong danh sách chưa và không phải sản phẩm hiện tại
                            if (!product.getProduct_id().equals(productID) &&
                                    !isProductAlreadyAdded(product.getProduct_id())) {
                                recommendedProducts.add(product);
                            }
                        }

                        // Cập nhật adapter
                        recommendationAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("DetailedProduct", "Error querying by tag: " + tag, e);
                    });
        }
    }

    // Kiểm tra sản phẩm đã được thêm vào danh sách đề xuất chưa
    private boolean isProductAlreadyAdded(String productId) {
        for (Products product : recommendedProducts) {
            if (product.getProduct_id().equals(productId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up resources if needed
        if (recommendedProducts != null) {
            recommendedProducts.clear();
        }
    }
}