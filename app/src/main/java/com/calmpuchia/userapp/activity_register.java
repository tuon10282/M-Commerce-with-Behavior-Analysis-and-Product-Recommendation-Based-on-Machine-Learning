package com.calmpuchia.userapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.calmpuchia.userapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class activity_register extends AppCompatActivity {
    private EditText edtname, edtphone, edtemail, edtpassword;
    private Button btnregister;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Ánh xạ view
        edtname = findViewById(R.id.edtname);
        edtphone = findViewById(R.id.edtphone);
        edtemail = findViewById(R.id.edtemail);
        edtpassword = findViewById(R.id.edtpassword);
        btnregister = findViewById(R.id.btnregister);

        btnregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void register() {
        String name = edtname.getText().toString().trim();
        String phone = edtphone.getText().toString().trim();
        String email = edtemail.getText().toString().trim();
        String pass = edtpassword.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Vui lòng nhập họ tên!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Vui lòng nhập số điện thoại!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Vui lòng nhập email!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Vui lòng nhập password!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo tài khoản với Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        String userId = firebaseUser.getUid();

                        // Gửi email xác thực
                        sendEmailVerification(firebaseUser, name, phone, email, pass, userId);
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Lỗi không xác định";
                        Toast.makeText(getApplicationContext(),
                                "Tạo tài khoản không thành công: " + errorMessage,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendEmailVerification(FirebaseUser firebaseUser, String name, String phone, String email, String pass, String userId) {
        firebaseUser.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Lưu thông tin user vào Firestore (có thể đánh dấu chưa verify)
                        saveUserToFirestore(name, phone, email, pass, userId, false);

                        Toast.makeText(getApplicationContext(),
                                "Tài khoản đã được tạo! Vui lòng kiểm tra email để xác thực.",
                                Toast.LENGTH_LONG).show();

                        // Chuyển về màn hình login
                        startActivity(new Intent(activity_register.this, activity_login.class));
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Không thể gửi email xác thực: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToFirestore(String name, String phone, String email, String pass, String userId, boolean isVerified) {
        // Tạo object User
        User user = new User(name, phone, email, pass);
        user.setUserId(userId);
        // Có thể thêm field isEmailVerified vào User model
        // user.setEmailVerified(isVerified);

        // Lưu vào Firestore
        db.collection("users")
                .document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    // Thành công - đã thông báo ở trên
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(),
                            "Không thể lưu thông tin người dùng: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}