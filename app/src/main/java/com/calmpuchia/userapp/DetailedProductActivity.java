package com.calmpuchia.userapp;

import android.os.Bundle;
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

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class DetailedProductActivity extends AppCompatActivity {
    private Button addToCartButton;
    private ImageView productImage;
    private ImageButton btnIncrease, btnDecrease;
    private TextView productPrice, productDescription, productName, numberText;
    private String productID = "";
    private int quantity = 1;
    private double unitPrice = 0.0;
    private String name = "";
    private String image = "";
    private ArrayList<String> descriptionList;

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

        addViews();
        bindData();
        addEvents();
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
        productPrice.setText("Giá: " + unitPrice + " đ");
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

        // Hiển thị hình ảnh bằng Glide
        Glide.with(this)
                .load(image)
                .placeholder(R.drawable.ic_placeholder) // bạn có thể thay icon này nếu muốn
                .into(productImage);
    }

    private void addEvents() {
        btnIncrease.setOnClickListener(v -> {
            quantity++;
            numberText.setText(String.valueOf(quantity));
        });

        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                numberText.setText(String.valueOf(quantity));
            }
        });

        addToCartButton.setOnClickListener(v -> {
            Toast.makeText(this, quantity + " sản phẩm đã được thêm vào giỏ!", Toast.LENGTH_SHORT).show();
            // TODO: Gọi API thêm vào giỏ hàng hoặc lưu vào Firestore
        });
    }
}
