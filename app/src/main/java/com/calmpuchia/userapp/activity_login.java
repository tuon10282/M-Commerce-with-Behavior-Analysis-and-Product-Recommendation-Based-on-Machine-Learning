package com.calmpuchia.userapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class activity_login extends AppCompatActivity {
    private EditText edtemail, edtpassword;
    private Button btnlogin, btnregister;
    private CheckBox chkSaveLogin;
    private TextView tvForgotPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        addViews();
        addEvents();
        restoreLoginInformation();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void addViews() {
        edtemail = findViewById(R.id.edtemail);
        edtpassword = findViewById(R.id.edtpassword);
        btnlogin = findViewById(R.id.btnlogin);
        btnregister = findViewById(R.id.btnregister);
        chkSaveLogin = findViewById(R.id.chkSaveLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
    }

    private void addEvents() {
        btnlogin.setOnClickListener(v -> login());
        btnregister.setOnClickListener(v -> {
            Intent intent = new Intent(activity_login.this, activity_register.class);
            startActivity(intent);
        });
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void login() {
        String email = edtemail.getText().toString().trim();
        String pass = edtpassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Please enter your password!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();

                if (user != null && user.isEmailVerified()) {
                    Toast.makeText(getApplicationContext(), "Login successful", Toast.LENGTH_SHORT).show();
                    saveLoginInformation();
                    Intent intent = new Intent(activity_login.this, HomepageActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please verify your email before logging in!",
                            Toast.LENGTH_LONG).show();
                    showResendVerificationDialog(user);
                }
            } else {
                String errorMessage = task.getException() != null ?
                        task.getException().getMessage() : "Unknown error";
                Toast.makeText(getApplicationContext(),
                        "Login failed: " + errorMessage,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showResendVerificationDialog(FirebaseUser user) {
        new AlertDialog.Builder(this)
                .setTitle("Email not verified")
                .setMessage("Would you like to resend the verification email?")
                .setPositiveButton("Resend", (dialog, which) -> {
                    if (user != null) {
                        user.sendEmailVerification()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(),
                                                "Verification email has been resent!",
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(),
                                                "Failed to resend email: " + task.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Forgot Password");

        final EditText input = new EditText(this);
        input.setHint("Enter your email");
        input.setPadding(50, 30, 50, 30);
        builder.setView(input);

        builder.setPositiveButton("Send", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Please enter your email!", Toast.LENGTH_SHORT).show();
            } else {
                resetPassword(email);
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void resetPassword(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(),
                                "Password reset email has been sent to " + email,
                                Toast.LENGTH_LONG).show();
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Unknown error";
                        Toast.makeText(getApplicationContext(),
                                "Failed to send password reset email: " + errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveLoginInformation() {
        SharedPreferences preferences = getSharedPreferences("LOGIN_PREFERENCE", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String usr = edtemail.getText().toString();
        String pwd = edtpassword.getText().toString();
        boolean isSave = chkSaveLogin.isChecked();

        if (isSave) {
            editor.putString("USER_NAME", usr);
            editor.putString("PASSWORD", pwd);
            editor.putBoolean("SAVED", true);
        } else {
            editor.clear();
        }

        editor.apply();
    }

    private void restoreLoginInformation() {
        SharedPreferences preferences = getSharedPreferences("LOGIN_PREFERENCE", MODE_PRIVATE);
        String email = preferences.getString("USER_NAME", "");
        String pass = preferences.getString("PASSWORD", "");
        boolean isSave = preferences.getBoolean("SAVED", false);

        if (isSave) {
            edtemail.setText(email);
            edtpassword.setText(pass);
            chkSaveLogin.setChecked(true);
        }
    }
}
