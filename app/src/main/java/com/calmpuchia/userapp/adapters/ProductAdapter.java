package com.calmpuchia.userapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
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
import java.util.Random;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private Context context;
    private List<Products> productList;

    public ProductAdapter(Context context, List<Products> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Products product = productList.get(position);

        // Set product name
        holder.productName.setText(product.getName());

        // Handle discount price
        boolean hasDiscount = product.getDiscount_price() > 0 &&
                product.getDiscount_price() < product.getPrice();

        if (hasDiscount) {
            // Show discount price as main price
            holder.productPrice.setText("$" + String.format("%,.0f", product.getDiscount_price()));

            // Show original price with strikethrough
            holder.originalPrice.setText("$" + String.format("%,.0f", product.getPrice()));
            holder.originalPrice.setPaintFlags(holder.originalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.originalPrice.setVisibility(View.VISIBLE);

            // Calculate and show discount percentage
            double discountPercent = ((product.getPrice() - product.getDiscount_price()) / product.getPrice()) * 100;
            holder.discountBadge.setText("-" + Math.round(discountPercent) + "%");
            holder.discountBadge.setVisibility(View.VISIBLE);
        } else {
            // Show regular price
            holder.productPrice.setText("$" + String.format("%,.0f", product.getPrice()));
            holder.originalPrice.setVisibility(View.GONE);
            holder.discountBadge.setVisibility(View.GONE);
        }

        // Set random sold count for demo (hoặc thay bằng data thật từ Firebase)
        Random random = new Random();
        int soldCount = random.nextInt(999) + 1;
        if (soldCount > 100) {
            holder.soldCount.setText("Đã bán " + soldCount + "+");
        } else {
            holder.soldCount.setText("Đã bán " + soldCount);
        }

        // Load product image with Picasso
        if (product.getImage_url() != null && !product.getImage_url().isEmpty()) {
            Picasso.get()
                    .load(product.getImage_url())
                    .placeholder(android.R.drawable.ic_menu_gallery) // placeholder khi đang load
                    .error(android.R.drawable.ic_menu_gallery) // image khi lỗi
                    .into(holder.productImage);
        } else {
            holder.productImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Set click listener để chuyển sang DetailedProductActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailedProductActivity.class);
            intent.putExtra("pid", product.getProduct_id());
            intent.putExtra("name", product.getName());
            intent.putExtra("price", product.getPrice());
            intent.putExtra("discount_price", product.getDiscount_price()); // Thêm discount price
            intent.putExtra("image", product.getImage_url());
            intent.putStringArrayListExtra("description", new ArrayList<>(product.getDescription()));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    // Method để update data
    public void updateData(List<Products> newProductList) {
        this.productList.clear();
        this.productList.addAll(newProductList);
        notifyDataSetChanged();
    }

    // ViewHolder class
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, originalPrice, discountBadge, soldCount;
        ImageView productImage;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            originalPrice = itemView.findViewById(R.id.originalPrice);
            discountBadge = itemView.findViewById(R.id.discountBadge);
            soldCount = itemView.findViewById(R.id.soldCount);
            productImage = itemView.findViewById(R.id.productImage);
        }
    }
}