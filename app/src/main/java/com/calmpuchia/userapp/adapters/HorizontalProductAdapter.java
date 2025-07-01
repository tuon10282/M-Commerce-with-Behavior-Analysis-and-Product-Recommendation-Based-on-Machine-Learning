package com.calmpuchia.userapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.calmpuchia.userapp.DetailedProductActivity;
import com.calmpuchia.userapp.R;
import com.calmpuchia.userapp.models.Products;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class HorizontalProductAdapter extends RecyclerView.Adapter<HorizontalProductAdapter.RelatedProductViewHolder> {
    private static final String TAG = "HorizontalProductAdapter";
    private Context context;
    private List<Products> productList;

    public HorizontalProductAdapter(Context context, List<Products> productList) {
        this.context = context;
        this.productList = productList != null ? productList : new ArrayList<>();
    }

    @NonNull
    @Override
    public RelatedProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_homepage, parent, false);
        return new RelatedProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RelatedProductViewHolder holder, int position) {
        if (productList == null || position >= productList.size()) {
            Log.e(TAG, "Invalid position or null productList");
            return;
        }

        Products product = productList.get(position);
        if (product == null) {
            Log.e(TAG, "Product is null at position: " + position);
            return;
        }

        // Debug log
        Log.d(TAG, "Binding product: " + product.getName() + " - Price: " + product.getPrice());

        // Set product name với kiểm tra null
        String productName = product.getName();
        if (productName != null && !productName.trim().isEmpty()) {
            holder.productName.setText(productName);
            holder.productName.setVisibility(View.VISIBLE);
        } else {
            holder.productName.setText("Sản phẩm không tên");
            holder.productName.setVisibility(View.VISIBLE);
        }

        // Set price với format số và kiểm tra giá trị
        try {
            String priceText;
            double displayPrice;

            if (product.getDiscount_price() > 0 && product.getDiscount_price() < product.getPrice()) {
                displayPrice = product.getDiscount_price();
            } else {
                displayPrice = product.getPrice();
            }

            // Format price với dấu phẩy phân cách hàng nghìn
            priceText = String.format("%,.0f$", displayPrice);
            holder.productPrice.setText(priceText);
            holder.productPrice.setVisibility(View.VISIBLE);

            Log.d(TAG, "Price set: " + priceText);
        } catch (Exception e) {
            Log.e(TAG, "Error formatting price: " + e.getMessage());
            holder.productPrice.setText("Giá: Liên hệ");
            holder.productPrice.setVisibility(View.VISIBLE);
        }

        // Load image với error handling tốt hơn
        if (product.getImage_url() != null && !product.getImage_url().trim().isEmpty()) {
            Picasso.get()
                    .load(product.getImage_url())
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_placeholder)
                    .fit()
                    .centerCrop()
                    .into(holder.productImage, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Image loaded successfully for: " + product.getName());
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e(TAG, "Error loading image: " + e.getMessage());
                        }
                    });
        } else {
            holder.productImage.setImageResource(R.drawable.ic_placeholder);
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(context, DetailedProductActivity.class);
                intent.putExtra("pid", product.getProduct_id());
                intent.putExtra("name", product.getName());
                intent.putExtra("price", product.getPrice());
                intent.putExtra("image", product.getImage_url());

                if (product.getDescription() != null) {
                    intent.putStringArrayListExtra("description", new ArrayList<>(product.getDescription()));
                }

                context.startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error starting DetailedProductActivity: " + e.getMessage());
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    // Method để update data
    public void updateProductList(List<Products> newProductList) {
        this.productList = newProductList != null ? newProductList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class RelatedProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice;
        ImageView productImage;

        public RelatedProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productImage = itemView.findViewById(R.id.productImage);

            // Kiểm tra xem các view có được tìm thấy không
            if (productName == null) {
                Log.e("ViewHolder", "productName TextView not found!");
            }
            if (productPrice == null) {
                Log.e("ViewHolder", "productPrice TextView not found!");
            }
            if (productImage == null) {
                Log.e("ViewHolder", "productImage ImageView not found!");
            }
        }
    }
}