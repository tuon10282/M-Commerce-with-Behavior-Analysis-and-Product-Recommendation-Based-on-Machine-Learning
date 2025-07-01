package com.calmpuchia.userapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class ProductReviewActivity extends AppCompatActivity {

    private static final String TAG = "ProductReviewActivity";
    private static final int MAX_CHAR_COUNT = 300;
    private static final int MAX_IMAGES = 3;

    private ImageView btnBack, btnClose;
    private ImageView imgProduct;
    private TextView txtProductName;
    private ImageView star1, star2, star3, star4, star5;
    private EditText edtReview;
    private TextView txtCharCount;
    private LinearLayout layoutAddMedia;
    private Button btnSubmit;

    private int currentRating = 0;
    private String orderId;
    private String productId;
    private String productName;
    private String productImage;
    private String userId;
    private List<Uri> selectedMediaUris = new ArrayList<>();
    private List<String> base64Images = new ArrayList<>();
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ActivityResultLauncher<Intent> mediaPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_review);

        initFirebase();
        initViews();
        getIntentData();
        setupListeners();
        setupMediaPicker();
        loadProductInfo();
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        btnClose = findViewById(R.id.btn_close);
        imgProduct = findViewById(R.id.img_product);
        txtProductName = findViewById(R.id.txt_product_name);

        star1 = findViewById(R.id.star1);
        star2 = findViewById(R.id.star2);
        star3 = findViewById(R.id.star3);
        star4 = findViewById(R.id.star4);
        star5 = findViewById(R.id.star5);

        edtReview = findViewById(R.id.edt_review);
        txtCharCount = findViewById(R.id.txt_char_count);
        layoutAddMedia = findViewById(R.id.layout_add_media);
        btnSubmit = findViewById(R.id.btn_submit);

        // Khởi tạo character count
        txtCharCount.setText("0/" + MAX_CHAR_COUNT);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        orderId = intent.getStringExtra("order_id");
        productId = intent.getStringExtra("product_id");
        productName = intent.getStringExtra("product_name");
        productImage = intent.getStringExtra("product_image");

        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Received data - Order ID: " + orderId +
                ", Product ID: " + productId +
                ", Product Name: " + productName +
                ", Product Image: " + productImage);

        // Validate required data
        if (orderId == null || productId == null || productName == null) {
            Toast.makeText(this, "Thông tin sản phẩm không đầy đủ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void loadProductInfo() {
        // Hiển thị tên sản phẩm
        if (productName != null) {
            txtProductName.setText(productName);
        }

        // Load ảnh sản phẩm nếu có
        if (productImage != null && !productImage.isEmpty()) {
            Glide.with(this)
                    .load(productImage)
                    .into(imgProduct);
        } else {
            // Nếu không có ảnh, hiển thị ảnh mặc định
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnClose.setOnClickListener(v -> finish());

        star1.setOnClickListener(v -> setRating(1));
        star2.setOnClickListener(v -> setRating(2));
        star3.setOnClickListener(v -> setRating(3));
        star4.setOnClickListener(v -> setRating(4));
        star5.setOnClickListener(v -> setRating(5));

        edtReview.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s.length();
                txtCharCount.setText(length + "/" + MAX_CHAR_COUNT);

                // Giới hạn số ký tự
                if (length > MAX_CHAR_COUNT) {
                    edtReview.setText(s.subSequence(0, MAX_CHAR_COUNT));
                    edtReview.setSelection(MAX_CHAR_COUNT);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        layoutAddMedia.setOnClickListener(v -> openMediaPicker());
        btnSubmit.setOnClickListener(v -> submitReview());
    }

    private void setupMediaPicker() {
        mediaPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data != null) {
                                selectedMediaUris.clear();
                                base64Images.clear();
                                layoutAddMedia.removeAllViews();

                                List<Uri> tempUris = new ArrayList<>();

                                if (data.getClipData() != null) {
                                    int count = Math.min(data.getClipData().getItemCount(), MAX_IMAGES);
                                    for (int i = 0; i < count; i++) {
                                        Uri uri = data.getClipData().getItemAt(i).getUri();
                                        tempUris.add(uri);
                                    }
                                } else if (data.getData() != null) {
                                    tempUris.add(data.getData());
                                }

                                // Giới hạn tối đa 3 ảnh
                                int imagesToProcess = Math.min(tempUris.size(), MAX_IMAGES);
                                for (int i = 0; i < imagesToProcess; i++) {
                                    Uri uri = tempUris.get(i);
                                    selectedMediaUris.add(uri);
                                    convertToBase64(uri);
                                    addPreviewImage(uri);
                                }

                                String message = imagesToProcess == MAX_IMAGES && tempUris.size() > MAX_IMAGES
                                        ? "Đã chọn " + imagesToProcess + " ảnh (tối đa " + MAX_IMAGES + " ảnh)"
                                        : "Đã chọn " + imagesToProcess + " ảnh";

                                Toast.makeText(ProductReviewActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );
    }

    private void openMediaPicker() {
        if (selectedMediaUris.size() >= MAX_IMAGES) {
            Toast.makeText(this, "Bạn chỉ có thể chọn tối đa " + MAX_IMAGES + " ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        mediaPickerLauncher.launch(intent);
    }

    private void convertToBase64(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            // Resize ảnh để giảm kích thước
            Bitmap resizedBitmap = resizeBitmap(bitmap, 800, 600);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String base64String = Base64.encodeToString(byteArray, Base64.DEFAULT);

            base64Images.add(base64String);

            // Đóng stream
            inputStream.close();
            byteArrayOutputStream.close();

        } catch (IOException e) {
            Log.e(TAG, "Error converting image to base64", e);
            Toast.makeText(this, "Có lỗi khi xử lý ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float ratio = Math.min((float) maxWidth / width, (float) maxHeight / height);

        if (ratio < 1) {
            int newWidth = Math.round(width * ratio);
            int newHeight = Math.round(height * ratio);
            return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        }

        return bitmap;
    }

    private void addPreviewImage(Uri imageUri) {
        // Tạo container cho ảnh và nút xóa
        LinearLayout imageContainer = new LinearLayout(this);
        imageContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        containerParams.setMargins(16, 0, 16, 0);
        imageContainer.setLayoutParams(containerParams);

        // Tạo ImageView
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(250, 250);
        imageView.setLayoutParams(imageParams);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageURI(imageUri);

        // Tạo nút xóa
        Button removeButton = new Button(this);
        removeButton.setText("Xóa");
        removeButton.setTextSize(12);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        removeButton.setLayoutParams(buttonParams);
        removeButton.setOnClickListener(v -> removeImage(imageContainer, imageUri));

        // Thêm ImageView và Button vào container
        imageContainer.addView(imageView);
        imageContainer.addView(removeButton);

        // Thêm container vào layout chính
        layoutAddMedia.addView(imageContainer);
    }

    private void removeImage(LinearLayout imageContainer, Uri imageUri) {
        int index = selectedMediaUris.indexOf(imageUri);
        if (index != -1) {
            selectedMediaUris.remove(index);
            if (index < base64Images.size()) {
                base64Images.remove(index);
            }
            layoutAddMedia.removeView(imageContainer);
            Toast.makeText(this, "Đã xóa ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void setRating(int rating) {
        currentRating = rating;
        updateStarDisplay();
    }

    private void updateStarDisplay() {
        ImageView[] stars = {star1, star2, star3, star4, star5};
        for (int i = 0; i < stars.length; i++) {
            stars[i].setImageResource(i < currentRating ? R.mipmap.ic_star : R.mipmap.ic_star_empty);
        }
    }

    private void submitReview() {
        if (currentRating == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        String reviewText = edtReview.getText().toString().trim();
        if (reviewText.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nội dung đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Đang gửi...");

        submitReviewToFirestore(reviewText);
    }

    private void submitReviewToFirestore(String reviewText) {
        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("content", reviewText);
        reviewData.put("created_at", getCurrentTimestamp());
        reviewData.put("feedback_id", generateFeedbackId());
        reviewData.put("order_id", orderId);

        // Parse productId to Long, handle potential errors
        try {
            reviewData.put("product_id", Long.parseLong(productId));
        } catch (NumberFormatException e) {
            reviewData.put("product_id", productId); // Keep as string if can't parse
        }

        reviewData.put("ratings", currentRating);

        // Parse userId to Long, handle potential errors
        try {
            reviewData.put("user_id", Long.parseLong(userId));
        } catch (NumberFormatException e) {
            reviewData.put("user_id", userId); // Keep as string if can't parse
        }

        // Thêm base64 images nếu có
        if (!base64Images.isEmpty()) {
            reviewData.put("images", base64Images);
        }

        // Submit review to Firestore
        db.collection("product_feedback")
                .add(reviewData)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Review submitted successfully");

                    // Cập nhật trạng thái reviewed trong order items
                    updateOrderItemReviewStatus();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error submitting review", e);
                    Toast.makeText(ProductReviewActivity.this,
                            "Có lỗi xảy ra khi gửi đánh giá. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Gửi");
                });
    }

    private void updateOrderItemReviewStatus() {
        db.collection("orders")
                .document(orderId)
                .collection("items")
                .whereEqualTo("product_id", productId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().update("reviewed", true);
                    }

                    Toast.makeText(ProductReviewActivity.this,
                            "Đánh giá đã được gửi thành công!", Toast.LENGTH_SHORT).show();

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("review_submitted", true);
                    resultIntent.putExtra("reviewed_product_id", productId);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating order item review status", e);
                    // Vẫn coi như thành công vì review đã được submit
                    Toast.makeText(ProductReviewActivity.this,
                            "Đánh giá đã được gửi thành công!", Toast.LENGTH_SHORT).show();

                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("review_submitted", true);
                    resultIntent.putExtra("reviewed_product_id", productId);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                });
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private String generateFeedbackId() {
        return "fb" + System.currentTimeMillis() + String.format("%03d", (int) (Math.random() * 1000));
    }
}