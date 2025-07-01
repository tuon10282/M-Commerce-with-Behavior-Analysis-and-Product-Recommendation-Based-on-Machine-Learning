package com.calmpuchia.userapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.calmpuchia.userapp.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class EditUserActivity extends AppCompatActivity {

    private static final int GALLERY_PICK = 1;
    private static final String TAG = "EditUserActivity";

    private ImageView profileImageView;
    private TextInputEditText fullNameEditText, userPhoneEditText, addressEditText;
    private MaterialButton profileChangeBtn, saveBtn;

    private Uri imageUri;
    private String imageBase64 = "";
    private boolean imageChanged = false;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference userDocRef;
    private ListenerRegistration userListener;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_user);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();
        userDocRef = db.collection("users").document(currentUserId);

        initViews();
        loadUserInfo();
        setClickListeners();
    }

    private void initViews() {
        profileImageView = findViewById(R.id.settings_profile_image);
        fullNameEditText = findViewById(R.id.settings_full_name);
        userPhoneEditText = findViewById(R.id.settings_phone_number);
        addressEditText = findViewById(R.id.settings_address);
        profileChangeBtn = findViewById(R.id.profile_image_change_btn);
        saveBtn = findViewById(R.id.save_settings_btn);
    }

    private void setClickListeners() {
        profileChangeBtn.setOnClickListener(view -> openGallery());
        saveBtn.setOnClickListener(view -> saveUserInfo());
    }

    private void openGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_PICK);
    }

    private void loadUserInfo() {
        if (currentUserId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Sử dụng Firestore listener để lắng nghe thay đổi real-time
        userListener = userDocRef.addSnapshotListener((documentSnapshot, error) -> {
            if (error != null) {
                Log.e(TAG, "Listen failed: " + error.getMessage());
                Toast.makeText(EditUserActivity.this, "Failed to load user data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                // Chuyển đổi document thành User object
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    populateUserData(user);
                } else {
                    // Fallback: đọc từng field một
                    loadUserDataManually(documentSnapshot);
                }
            } else {
                Log.d(TAG, "User document does not exist");
                Toast.makeText(EditUserActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUserData(User user) {
        fullNameEditText.setText(user.getName());
        userPhoneEditText.setText(user.getPhone());
        addressEditText.setText(user.getAddress());

        if (user.getImage() != null && !user.getImage().isEmpty()) {
            imageBase64 = user.getImage();
            loadImageFromBase64(user.getImage());
        }
    }

    private void loadUserDataManually(DocumentSnapshot documentSnapshot) {
        String name = documentSnapshot.getString("name");
        String phone = documentSnapshot.getString("phone");
        String address = documentSnapshot.getString("address");
        String image = documentSnapshot.getString("image");

        fullNameEditText.setText(name != null ? name : "");
        userPhoneEditText.setText(phone != null ? phone : "");
        addressEditText.setText(address != null ? address : "");

        if (image != null && !image.isEmpty()) {
            imageBase64 = image;
            loadImageFromBase64(image);
        }
    }

    private void loadImageFromBase64(String base64Image) {
        if (base64Image.startsWith("data:image")) {
            // Nếu là data URL, load trực tiếp
            Glide.with(EditUserActivity.this)
                    .load(base64Image)
                    .placeholder(R.mipmap.profile)
                    .error(R.mipmap.profile)
                    .into(profileImageView);
        } else if (base64Image.startsWith("http")) {
            // Nếu là URL, load từ URL
            Glide.with(EditUserActivity.this)
                    .load(base64Image)
                    .placeholder(R.mipmap.profile)
                    .error(R.mipmap.profile)
                    .into(profileImageView);
        } else {
            // Nếu là Base64 thuần, thêm data URL prefix
            String dataUrl = "data:image/jpeg;base64," + base64Image;
            Glide.with(EditUserActivity.this)
                    .load(dataUrl)
                    .placeholder(R.mipmap.profile)
                    .error(R.mipmap.profile)
                    .into(profileImageView);
        }
    }

    private void saveUserInfo() {
        if (!validateInput()) return;

        if (imageChanged && imageUri != null) {
            // Convert image to Base64 and save
            convertImageToBase64AndSave();
        } else {
            // Update user info without changing image
            updateUserInfoToFirestore();
        }
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(fullNameEditText.getText().toString().trim())) {
            fullNameEditText.setError("Name is required");
            return false;
        }
        if (TextUtils.isEmpty(addressEditText.getText().toString().trim())) {
            addressEditText.setError("Address is required");
            return false;
        }
        if (TextUtils.isEmpty(userPhoneEditText.getText().toString().trim())) {
            userPhoneEditText.setError("Phone is required");
            return false;
        }
        return true;
    }

    /**
     * Tự động crop ảnh thành hình vuông và convert thành Base64
     */
    private void convertImageToBase64AndSave() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Processing image...");
        progressDialog.setMessage("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        // Run in background thread to avoid UI blocking
        new Thread(() -> {
            try {
                // Load bitmap từ URI
                Bitmap originalBitmap = loadBitmapFromUri(imageUri);

                if (originalBitmap == null) {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(EditUserActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                // Tự động crop thành hình vuông từ giữa
                Bitmap croppedBitmap = cropToSquare(originalBitmap);

                // Resize để tối ưu kích thước
                Bitmap resizedBitmap = resizeBitmap(croppedBitmap, 512);

                // Compress và convert thành Base64
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                byte[] imageBytes = baos.toByteArray();
                String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                // Cleanup bitmaps
                if (originalBitmap != croppedBitmap) originalBitmap.recycle();
                if (croppedBitmap != resizedBitmap) croppedBitmap.recycle();
                resizedBitmap.recycle();

                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    // Store as data URL for consistent handling
                    imageBase64 = "data:image/jpeg;base64," + base64Image;

                    // Preview ảnh đã crop
                    Glide.with(EditUserActivity.this)
                            .load(imageBase64)
                            .into(profileImageView);

                    updateUserInfoToFirestore();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(EditUserActivity.this, "Failed to process image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    /**
     * Load bitmap từ URI với xử lý rotation
     */
    private Bitmap loadBitmapFromUri(Uri uri) throws IOException {
        // Load bitmap
        InputStream inputStream = getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();

        if (bitmap == null) return null;

        // Xử lý rotation dựa trên EXIF data
        try {
            InputStream exifInputStream = getContentResolver().openInputStream(uri);
            ExifInterface exif = new ExifInterface(exifInputStream);
            exifInputStream.close();

            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            bitmap = rotateBitmap(bitmap, orientation);
        } catch (Exception e) {
            Log.w(TAG, "Could not read EXIF data", e);
        }

        return bitmap;
    }

    /**
     * Rotate bitmap theo EXIF orientation
     */
    private Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            default:
                return bitmap;
        }

        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (rotatedBitmap != bitmap) {
            bitmap.recycle();
        }
        return rotatedBitmap;
    }

    /**
     * Tự động crop bitmap thành hình vuông từ center
     */
    private Bitmap cropToSquare(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Nếu đã là hình vuông thì return luôn
        if (width == height) {
            return bitmap;
        }

        // Tìm kích thước nhỏ nhất để làm kích thước hình vuông
        int size = Math.min(width, height);

        // Tính toán vị trí crop từ center
        int x = (width - size) / 2;
        int y = (height - size) / 2;

        // Crop bitmap
        Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, x, y, size, size);

        return croppedBitmap;
    }

    /**
     * Resize bitmap giữ nguyên tỷ lệ
     */
    private Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxSize && height <= maxSize) {
            return bitmap;
        }

        float ratio = Math.min((float) maxSize / width, (float) maxSize / height);
        int newWidth = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    private void updateUserInfoToFirestore() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Updating profile...");
        progressDialog.setMessage("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", fullNameEditText.getText().toString().trim());
        userMap.put("address", addressEditText.getText().toString().trim());
        userMap.put("phone", userPhoneEditText.getText().toString().trim());

        // Only update image if it was changed
        if (imageChanged && !imageBase64.isEmpty()) {
            userMap.put("image", imageBase64);
        }

        // Firestore tự động thêm timestamp
        userMap.put("lastUpdated", com.google.firebase.Timestamp.now());

        // Sử dụng set với merge option để không ghi đè toàn bộ document
        userDocRef.set(userMap, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressDialog.dismiss();
                        Toast.makeText(EditUserActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.e(TAG, "Error updating document", e);
                        Toast.makeText(EditUserActivity.this, "Database update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            if (imageUri != null) {
                // Set flag để biết ảnh đã thay đổi
                imageChanged = true;

                // Preview ảnh được chọn (chưa crop)
                Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.mipmap.profile)
                        .error(R.mipmap.profile)
                        .into(profileImageView);

                Toast.makeText(this, "Image selected. Click Save to apply changes.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy listener để tránh memory leak
        if (userListener != null) {
            userListener.remove();
        }
    }
}