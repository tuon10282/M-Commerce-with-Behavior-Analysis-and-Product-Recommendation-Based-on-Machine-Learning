package com.calmpuchia.userapp;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

public class HomepageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage); // luôn phải gọi trước

        View rootView = findViewById(R.id.root_view);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                // xử lý insets nếu cần
                return insets;
            });
        } else {
            Log.e("HomepageActivity", "rootView is null!");
        }
    }
}