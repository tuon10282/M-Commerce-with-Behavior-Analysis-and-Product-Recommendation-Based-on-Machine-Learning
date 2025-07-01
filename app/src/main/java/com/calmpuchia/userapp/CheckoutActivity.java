package com.calmpuchia.userapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.calmpuchia.userapp.adapters.CheckoutAdapter;
import com.calmpuchia.userapp.models.CartItem;
import com.calmpuchia.userapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.FieldValue;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CheckoutActivity extends AppCompatActivity {
    private static final String TAG = "CheckoutActivity";
    private static final String GHN_TOKEN = "6b156082-548d-11f0-ba75-a6ca4deb76d8";
    private static final String GHN_SHOP_ID = "4832502";
    private static final double USD_TO_VND_RATE = 26000.0;

    // International coin system - 1% of order total directly
    private static final double COIN_RATE = 0.01; // 1% of order total in USD

    // GHN API endpoints
    private static final String GHN_BASE_URL = "https://online-gateway.ghn.vn/shiip/public-api";
    private static final String GHN_CALCULATE_FEE_URL = GHN_BASE_URL + "/v2/shipping-order/fee";
    private static final String GHN_AVAILABLE_SERVICES_URL = GHN_BASE_URL + "/v2/shipping-order/available-services";

    private TextView tvUserName, tvUserAddress, tvSubtotal, tvShippingFee, tvVoucherDiscount, tvFinalTotal, tvCoinsEarned;
    private Button btnEditAddress, btnApplyVoucher, btnCheckout;
    private EditText etVoucherCode;
    private RadioGroup rgPaymentMethod;
    private LinearLayout llPaymentDetails;
    private RecyclerView recyclerCheckoutItems;
    private ProgressBar progressShipping;

    private CheckoutAdapter checkoutAdapter;
    private List<CartItem> selectedItems = new ArrayList<>();
    private User currentUser;
    private String userAddress = "";

    private double subtotal = 0.0;
    private double shippingFeeUSD = 0.0;
    private double voucherDiscount = 0.0;
    private double finalTotal = 0.0;
    private int coinsToEarn = 0; // Coins user will earn from this order

    private FirebaseFirestore db;
    private String userId;
    private String storeId = "Store04";
    private OkHttpClient httpClient;

    private int fromDistrictId = 1542;
    private int toDistrictId = 0;
    private String toWardCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        initViews();
        initHttpClient();
        setupRecyclerView();
        loadUserInfo();
        calculatePrices();
        setupEventListeners();
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tvUserName);
        tvUserAddress = findViewById(R.id.tvUserAddress);
        btnEditAddress = findViewById(R.id.btnEditAddress);

        recyclerCheckoutItems = findViewById(R.id.recyclerCheckoutItems);

        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShippingFee = findViewById(R.id.tvShippingFee);
        tvVoucherDiscount = findViewById(R.id.tvVoucherDiscount);
        tvFinalTotal = findViewById(R.id.tvFinalTotal);
        tvCoinsEarned = findViewById(R.id.tvCoinsEarned); // Add this TextView to your layout

        etVoucherCode = findViewById(R.id.etVoucherCode);
        btnApplyVoucher = findViewById(R.id.btnApplyVoucher);

        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        llPaymentDetails = findViewById(R.id.llPaymentDetails);

        btnCheckout = findViewById(R.id.btnCheckout);
        progressShipping = findViewById(R.id.progressShipping);

        selectedItems = getIntent().getParcelableArrayListExtra("selectedItems");
        if (selectedItems == null) {
            selectedItems = new ArrayList<>();
        }

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private void initHttpClient() {
        httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    private void setupRecyclerView() {
        checkoutAdapter = new CheckoutAdapter(selectedItems);
        recyclerCheckoutItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerCheckoutItems.setAdapter(checkoutAdapter);
    }

    private void loadUserInfo() {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUser = documentSnapshot.toObject(User.class);
                        if (currentUser != null) {
                            tvUserName.setText(currentUser.getName());

                            if (!TextUtils.isEmpty(currentUser.getAddress())) {
                                userAddress = currentUser.getAddress();
                                tvUserAddress.setText(userAddress);
                                calculateShippingFee();
                            } else {
                                tvUserAddress.setText("No address yet");
                                showAddressDialog();
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user info", e);
                    Toast.makeText(this, "Error loading user info", Toast.LENGTH_SHORT).show();
                });
    }

    private void calculatePrices() {
        subtotal = 0.0;
        for (CartItem item : selectedItems) {
            subtotal += item.getPrice() * item.getQuantity();
        }

        finalTotal = subtotal + shippingFeeUSD - voucherDiscount;

        // Calculate coins to earn - 1% of final total directly (no currency conversion)
        coinsToEarn = (int) Math.floor(finalTotal * COIN_RATE);

        updatePriceDisplay();
    }

    private void updatePriceDisplay() {
        tvSubtotal.setText(String.format("$%.2f", subtotal));
        tvShippingFee.setText(String.format("$%.2f", shippingFeeUSD));
        tvVoucherDiscount.setText(String.format("-$%.2f", voucherDiscount));
        tvFinalTotal.setText(String.format("$%.2f", finalTotal));

        // Display coins to be earned
        if (tvCoinsEarned != null) {
            tvCoinsEarned.setText(String.format("You'll earn: %d coins", coinsToEarn));
            tvCoinsEarned.setVisibility(coinsToEarn > 0 ? View.VISIBLE : View.GONE);
        }

        btnCheckout.setText(String.format("Place Order - $%.2f", finalTotal));
    }

    private void calculateShippingFee() {
        if (TextUtils.isEmpty(userAddress)) {
            return;
        }

        if (progressShipping != null) {
            progressShipping.setVisibility(View.VISIBLE);
        }
        tvShippingFee.setText("Calculating...");

        toDistrictId = 1444;
        toWardCode = "20314";

        PackageDimensions totalDimensions = calculatePackageDimensions();

        getAvailableServices(fromDistrictId, toDistrictId, (serviceId) -> {
            if (serviceId > 0) {
                calculateShippingFeeWithService(serviceId, totalDimensions.weight,
                        totalDimensions.length, totalDimensions.width, totalDimensions.height);
            } else {
                calculateShippingFeeWithService(53321, totalDimensions.weight,
                        totalDimensions.length, totalDimensions.width, totalDimensions.height);
            }
        });
    }

    private PackageDimensions calculatePackageDimensions() {
        int totalWeight = 0;
        int maxLength = 0, maxWidth = 0, maxHeight = 0;

        for (CartItem item : selectedItems) {
            String categoryId = getCategoryIdFromItem(item);
            CategoryDimensions catDims = getCategoryDimensions(categoryId);

            totalWeight += catDims.weight * item.getQuantity();

            maxLength = Math.max(maxLength, catDims.length);
            maxWidth = Math.max(maxWidth, catDims.width);
            maxHeight = Math.max(maxHeight, catDims.height);
        }

        maxLength = Math.max(maxLength, 10);
        maxWidth = Math.max(maxWidth, 10);
        maxHeight = Math.max(maxHeight, 10);
        totalWeight = Math.max(totalWeight, 100);

        return new PackageDimensions(totalWeight, maxLength, maxWidth, maxHeight);
    }

    private String getCategoryIdFromItem(CartItem item) {
        String productName = item.getName().toLowerCase();

        if (productName.contains("lip")) return "149";
        if (productName.contains("oil")) return "160";
        if (productName.contains("hair")) return "166";
        if (productName.contains("skin") || productName.contains("cream")) return "178";
        if (productName.contains("eye")) return "194";
        if (productName.contains("nose")) return "200";
        if (productName.contains("cheek") || productName.contains("blush")) return "208";
        if (productName.contains("face")) return "213";
        if (productName.contains("nail")) return "214";
        if (productName.contains("eyebrow") || productName.contains("brow")) return "215";
        if (productName.contains("hand")) return "219";
        if (productName.contains("man") || productName.contains("men")) return "220";

        return "178";
    }

    private CategoryDimensions getCategoryDimensions(String categoryId) {
        switch (categoryId) {
            case "149": return new CategoryDimensions(50, 8, 8, 3);
            case "160": return new CategoryDimensions(200, 12, 5, 5);
            case "166": return new CategoryDimensions(300, 20, 6, 6);
            case "178": return new CategoryDimensions(150, 12, 8, 8);
            case "194": return new CategoryDimensions(30, 10, 3, 3);
            case "200": return new CategoryDimensions(20, 8, 3, 3);
            case "208": return new CategoryDimensions(40, 8, 8, 2);
            case "213": return new CategoryDimensions(100, 15, 10, 5);
            case "214": return new CategoryDimensions(25, 8, 3, 8);
            case "215": return new CategoryDimensions(15, 12, 2, 2);
            case "219": return new CategoryDimensions(80, 15, 5, 5);
            case "220": return new CategoryDimensions(200, 18, 8, 8);
            default: return new CategoryDimensions(100, 12, 8, 8);
        }
    }

    private static class CategoryDimensions {
        int weight, length, width, height;

        CategoryDimensions(int weight, int length, int width, int height) {
            this.weight = weight;
            this.length = length;
            this.width = width;
            this.height = height;
        }
    }

    private static class PackageDimensions {
        int weight, length, width, height;

        PackageDimensions(int weight, int length, int width, int height) {
            this.weight = weight;
            this.length = length;
            this.width = width;
            this.height = height;
        }
    }

    private void getAvailableServices(int fromDistrict, int toDistrict, ServiceCallback callback) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("shop_id", Integer.parseInt(GHN_SHOP_ID));
            requestBody.put("from_district", fromDistrict);
            requestBody.put("to_district", toDistrict);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    requestBody.toString()
            );

            Request request = new Request.Builder()
                    .url(GHN_AVAILABLE_SERVICES_URL)
                    .addHeader("Token", GHN_TOKEN)
                    .post(body)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Error getting available services", e);
                    runOnUiThread(() -> callback.onResult(0));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseBody = response.body().string();
                            JSONObject jsonResponse = new JSONObject(responseBody);

                            if (jsonResponse.getInt("code") == 200) {
                                JSONArray services = jsonResponse.getJSONArray("data");
                                if (services.length() > 0) {
                                    int serviceId = services.getJSONObject(0).getInt("service_id");
                                    runOnUiThread(() -> callback.onResult(serviceId));
                                    return;
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing services response", e);
                        }
                    }
                    runOnUiThread(() -> callback.onResult(0));
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error creating services request", e);
            callback.onResult(0);
        }
    }

    private void calculateShippingFeeWithService(int serviceId, int weight, int length, int width, int height) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("service_id", serviceId);
            requestBody.put("insurance_value", (int)(subtotal * USD_TO_VND_RATE));
            requestBody.put("coupon", JSONObject.NULL);
            requestBody.put("from_district_id", fromDistrictId);
            requestBody.put("to_district_id", toDistrictId);
            requestBody.put("to_ward_code", toWardCode);
            requestBody.put("height", height);
            requestBody.put("length", length);
            requestBody.put("width", width);
            requestBody.put("weight", weight);

            RequestBody body = RequestBody.create(
                    MediaType.parse("application/json"),
                    requestBody.toString()
            );

            Request request = new Request.Builder()
                    .url(GHN_CALCULATE_FEE_URL)
                    .addHeader("Token", GHN_TOKEN)
                    .addHeader("ShopId", GHN_SHOP_ID)
                    .post(body)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Error calculating shipping fee", e);
                    runOnUiThread(() -> {
                        hideShippingProgress();
                        shippingFeeUSD = 30000.0 / USD_TO_VND_RATE;
                        calculatePrices();
                        Toast.makeText(CheckoutActivity.this, "Using default shipping fee", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";

                    runOnUiThread(() -> {
                        hideShippingProgress();

                        if (response.isSuccessful()) {
                            try {
                                JSONObject jsonResponse = new JSONObject(responseBody);

                                if (jsonResponse.getInt("code") == 200) {
                                    JSONObject data = jsonResponse.getJSONObject("data");
                                    int totalFeeVND = data.getInt("total");
                                    shippingFeeUSD = totalFeeVND / USD_TO_VND_RATE;
                                    calculatePrices();
                                    Log.d(TAG, "Shipping fee calculated: " + totalFeeVND + " VND = $" + shippingFeeUSD);
                                } else {
                                    String message = jsonResponse.optString("message", "Unknown error");
                                    Log.e(TAG, "GHN API error: " + message);
                                    useDefaultShippingFee();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing shipping fee response: " + responseBody, e);
                                useDefaultShippingFee();
                            }
                        } else {
                            Log.e(TAG, "HTTP error: " + response.code() + " - " + responseBody);
                            useDefaultShippingFee();
                        }
                    });
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error creating shipping fee request", e);
            hideShippingProgress();
            useDefaultShippingFee();
        }
    }

    private void useDefaultShippingFee() {
        shippingFeeUSD = 30000.0 / USD_TO_VND_RATE;
        calculatePrices();
        Toast.makeText(this, "Using default shipping fee", Toast.LENGTH_SHORT).show();
    }

    private void hideShippingProgress() {
        if (progressShipping != null) {
            progressShipping.setVisibility(View.GONE);
        }
    }

    private void setupEventListeners() {
        btnEditAddress.setOnClickListener(v -> showAddressDialog());
        btnApplyVoucher.setOnClickListener(v -> applyVoucher());
        rgPaymentMethod.setOnCheckedChangeListener((group, checkedId) -> showPaymentDetails(checkedId));
        btnCheckout.setOnClickListener(v -> processCheckout());
    }

    private void showAddressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter delivery address");

        EditText etAddress = new EditText(this);
        etAddress.setText(userAddress);
        etAddress.setHint("Enter your address...");
        builder.setView(etAddress);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String address = etAddress.getText().toString().trim();
            if (!TextUtils.isEmpty(address)) {
                userAddress = address;
                tvUserAddress.setText(userAddress);

                Map<String, Object> updates = new HashMap<>();
                updates.put("address", address);

                db.collection("users").document(userId)
                        .update(updates)
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Address updated successfully");
                            calculateShippingFee();
                        })
                        .addOnFailureListener(e ->
                                Log.e(TAG, "Error updating address", e));
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void applyVoucher() {
        String voucherCode = etVoucherCode.getText().toString().trim();
        if (TextUtils.isEmpty(voucherCode)) {
            Toast.makeText(this, "Please enter voucher code", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("vouchers").document(voucherCode)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long discount = documentSnapshot.getLong("discount");
                        if (discount != null) {
                            voucherDiscount = discount.doubleValue() / USD_TO_VND_RATE;
                            calculatePrices();
                            Toast.makeText(this, "Voucher applied successfully!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Invalid voucher code", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking voucher", e);
                    Toast.makeText(this, "Error checking voucher", Toast.LENGTH_SHORT).show();
                });
    }

    private void showPaymentDetails(int checkedId) {
        llPaymentDetails.removeAllViews();

        if (checkedId == R.id.rbMomo) {
            TextView tvMomoInfo = new TextView(this);
            tvMomoInfo.setText("Account holder: Tran Bao Ngoc\nPhone: 0933109239");
            tvMomoInfo.setPadding(16, 8, 16, 8);
            llPaymentDetails.addView(tvMomoInfo);
            llPaymentDetails.setVisibility(View.VISIBLE);
        } else if (checkedId == R.id.rbBankTransfer) {
            TextView tvBankInfo = new TextView(this);
            tvBankInfo.setText("Account holder: TRAN BAO NGOC\nAccount: 007072004\nBank: VIB - International Bank");
            tvBankInfo.setPadding(16, 8, 16, 8);
            llPaymentDetails.addView(tvBankInfo);
            llPaymentDetails.setVisibility(View.VISIBLE);
        } else {
            llPaymentDetails.setVisibility(View.GONE);
        }
    }

    private String generateOrderId() {
        Random random = new Random();
        int orderNumber = 1000000 + random.nextInt(9000000);
        return "OD" + orderNumber;
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void processCheckout() {
        if (TextUtils.isEmpty(userAddress)) {
            Toast.makeText(this, "Please enter delivery address", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedPaymentId = rgPaymentMethod.getCheckedRadioButtonId();
        if (selectedPaymentId == -1) {
            Toast.makeText(this, "Please select payment method", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedPayment = findViewById(selectedPaymentId);
        String paymentMethod = selectedPayment.getText().toString();

        String orderId = generateOrderId();
        String timestamp = getCurrentTimestamp();

        DocumentReference orderRef = db.collection("orders").document();

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("created_at", timestamp);
        orderData.put("order_id", orderId);
        orderData.put("payment_method", paymentMethod);
        orderData.put("status", "pending");
        orderData.put("store_id", storeId);
        orderData.put("total", finalTotal);
        orderData.put("currency", "USD");
        orderData.put("user_id", userId);
        orderData.put("customer_name", currentUser.getName());
        orderData.put("customer_address", userAddress);
        orderData.put("customer_phone", currentUser.getPhone());
        orderData.put("shipping_fee", shippingFeeUSD);
        orderData.put("voucher_code", etVoucherCode.getText().toString().trim());
        orderData.put("voucher_discount", voucherDiscount);

        // Add coin information
        orderData.put("coin_added", coinsToEarn);

        WriteBatch batch = db.batch();
        batch.set(orderRef, orderData);

        CollectionReference itemsRef = orderRef.collection("items");
        for (int i = 0; i < selectedItems.size(); i++) {
            CartItem item = selectedItems.get(i);

            Map<String, Object> itemData = new HashMap<>();
            itemData.put("price", item.getPrice());
            itemData.put("product_id", item.getProductID());
            itemData.put("quantity", (long) item.getQuantity());
            itemData.put("product_name", item.getName());
            itemData.put("product_image", item.getImageUrl());

            DocumentReference itemRef = itemsRef.document();
            batch.set(itemRef, itemData);
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Order created successfully");

                    // Add coins to user account
                    if (coinsToEarn > 0) {
                        addCoinsToUser(coinsToEarn);
                    }

                    Toast.makeText(this, "Order placed successfully! You earned " + coinsToEarn + " coins!", Toast.LENGTH_LONG).show();
                    removeCheckedItemsFromCart();
                    MyFirebaseMessagingService.createOrderNotification(userId, orderId, "confirmed");
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating order", e);
                    Toast.makeText(this, "Order error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void addCoinsToUser(int coinsToAdd) {
        DocumentReference userRef = db.collection("users").document(userId);

        // Use transaction to safely increment coins
        db.runTransaction(transaction -> {
            // Get current user data
            Map<String, Object> userDoc = transaction.get(userRef).getData();

            long currentCoins = 0;
            if (userDoc != null && userDoc.containsKey("coins")) {
                Object coinsObj = userDoc.get("coins");
                if (coinsObj instanceof Long) {
                    currentCoins = (Long) coinsObj;
                } else if (coinsObj instanceof Integer) {
                    currentCoins = ((Integer) coinsObj).longValue();
                }
            }

            // Update coins
            long newCoins = currentCoins + coinsToAdd;
            transaction.update(userRef, "coins", newCoins);

            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Coins added successfully: " + coinsToAdd);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error adding coins to user", e);
            // Still consider order successful even if coin update fails
        });
    }

    private void removeCheckedItemsFromCart() {
        // Using FieldValue.delete() approach - simpler and more efficient
        db.collection("carts")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the first (and should be only) cart document
                        DocumentReference cartRef = queryDocumentSnapshots.getDocuments().get(0).getReference();

                        // Create update map to delete specific items
                        Map<String, Object> updates = new HashMap<>();
                        for (CartItem item : selectedItems) {
                            if (item.getProductID() != null && !item.getProductID().isEmpty()) {
                                // Use dot notation to delete specific fields in the items map
                                updates.put("items." + item.getProductID(), FieldValue.delete());
                                Log.d(TAG, "Marking for removal: " + item.getProductID());
                            }
                        }

                        if (!updates.isEmpty()) {
                            // Update the cart document to remove selected items
                            cartRef.update(updates)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Cart items removed successfully");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error removing cart items", e);
                                    });
                        }
                    } else {
                        Log.w(TAG, "No cart document found for user");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error querying cart documents", e);
                });
    }
    private interface ServiceCallback {
        void onResult(int serviceId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (httpClient != null) {
            httpClient.dispatcher().executorService().shutdown();
        }
    }
}