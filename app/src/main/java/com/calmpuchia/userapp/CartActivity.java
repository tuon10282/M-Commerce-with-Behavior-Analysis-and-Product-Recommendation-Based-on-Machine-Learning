package com.calmpuchia.userapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.calmpuchia.userapp.adapters.CartAdapter;
import com.calmpuchia.userapp.models.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CartActivity extends AppCompatActivity {
    private static final String TAG = "CartActivity";
    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems = new ArrayList<>();
    private TextView totalPriceText;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        initViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Load cart data
        loadCartFromFirestore();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerCart);
        totalPriceText = findViewById(R.id.totalPriceText);

        // Set checkout button click listener
        findViewById(R.id.btnCheckout).setOnClickListener(v -> proceedToCheckout());
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(cartItems, this::calculateTotal);
        recyclerView.setAdapter(cartAdapter);
    }

    private void loadCartFromFirestore() {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();

        db.collection("carts")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cartItems.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            // Get the items map from the document
                            Object itemsObject = document.get("items");

                            if (itemsObject instanceof Map) {
                                Map<String, Object> itemsMap = (Map<String, Object>) itemsObject;

                                // Iterate through each item in the items map
                                for (Map.Entry<String, Object> entry : itemsMap.entrySet()) {
                                    String productId = entry.getKey();
                                    Object itemObject = entry.getValue();

                                    if (itemObject instanceof Map) {
                                        Map<String, Object> itemData = (Map<String, Object>) itemObject;

                                        CartItem item = new CartItem();
                                        item.setProductID(productId);

                                        // Map item data to CartItem
                                        String name = (String) itemData.get("name");
                                        if (name != null) {
                                            item.setName(name);
                                        }

                                        String imageUrl = (String) itemData.get("image_url");
                                        if (imageUrl != null) {
                                            item.setImageUrl(imageUrl);
                                        }

                                        // Handle price - check for discount price first
                                        Object discountPriceObj = itemData.get("discount_price");
                                        Object regularPriceObj = itemData.get("price");

                                        if (discountPriceObj != null) {
                                            double discountPrice = ((Number) discountPriceObj).doubleValue();
                                            if (discountPrice > 0) {
                                                item.setDiscountPriceFromDouble(discountPrice);
                                                item.setHasDiscount(true);
                                            }
                                        }

                                        if (regularPriceObj != null) {
                                            double regularPrice = ((Number) regularPriceObj).doubleValue();
                                            item.setPriceFromDouble(regularPrice);
                                        }

                                        // Handle has_discount boolean
                                        Object hasDiscountObj = itemData.get("has_discount");
                                        if (hasDiscountObj instanceof Boolean) {
                                            item.setHasDiscount((Boolean) hasDiscountObj);
                                        }

                                        // Get quantity
                                        Object quantityObj = itemData.get("quantity");
                                        if (quantityObj != null) {
                                            int quantity = ((Number) quantityObj).intValue();
                                            item.setQuantity(quantity);
                                        } else {
                                            item.setQuantity(1);
                                        }

                                        // Set total price
                                        Object totalPriceObj = itemData.get("total_price");
                                        if (totalPriceObj != null) {
                                            double totalPrice = ((Number) totalPriceObj).doubleValue();
                                            item.setTotalPrice((int) Math.round(totalPrice * 100)); // Convert to cents
                                        }

                                        // Default selection state
                                        item.setSelected(false);

                                        // Validate item before adding
                                        if (item.isValid()) {
                                            cartItems.add(item);

                                            Log.d(TAG, "Added cart item: " + item.getName() +
                                                    ", Price: " + item.getEffectivePriceAsDouble() +
                                                    ", Quantity: " + item.getQuantity() +
                                                    ", Has Discount: " + item.isHasDiscount());
                                        } else {
                                            Log.w(TAG, "Invalid cart item, skipping: " + item.toString());
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing cart document: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }

                    // Update UI
                    cartAdapter.notifyDataSetChanged();
                    calculateTotal();

                    if (cartItems.isEmpty()) {
                        Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d(TAG, "Loaded " + cartItems.size() + " items from cart");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading cart: " + e.getMessage());
                    Toast.makeText(this, "Lỗi tải giỏ hàng: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
    private void calculateTotal() {
        int total = 0;
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                // Use effective price (discount price if available, otherwise regular price)
                total += item.getEffectiveTotalPrice();
            }
        }
        // Convert from cents to display format
        totalPriceText.setText("Tổng: " + String.format("%,d", total / 100) + " đ");
    }
    private void proceedToCheckout() {
        // Get selected items
        ArrayList<CartItem> selectedItems = new ArrayList<>();
        for (CartItem item : cartItems) {
            if (item.isSelected()) {
                selectedItems.add(item);
            }
        }

        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất một sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        // Go to CheckoutActivity
        Intent intent = new Intent(this, CheckoutActivity.class);
        intent.putParcelableArrayListExtra("selectedItems", selectedItems);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload cart when returning to this activity
        loadCartFromFirestore();
    }
}