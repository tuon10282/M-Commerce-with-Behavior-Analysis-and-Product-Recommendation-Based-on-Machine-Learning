package com.calmpuchia.userapp;

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

public class BestSellersAdapter extends RecyclerView.Adapter<BestSellersAdapter.ProductViewHolder> {
    private Context context;
    private List<Products> productList;

    public BestSellersAdapter(Context context, List<Products> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use layout for horizontal item or shared item_product
        View view = LayoutInflater.from(context).inflate(R.layout.item_nearby_deal_small, parent, false);
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

        // Set random sold count for demo
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
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(holder.productImage);
        } else {
            holder.productImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailedProductActivity.class);
            intent.putExtra("pid", product.getProduct_id());
            intent.putExtra("name", product.getName());
            intent.putExtra("price", product.getPrice());
            intent.putExtra("discount_price", product.getDiscount_price());
            intent.putExtra("image", product.getImage_url());
            intent.putStringArrayListExtra("description", new ArrayList<>(product.getDescription()));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    // Method to update the product list
    public void updateProductList(List<Products> newProductList) {
        this.productList = newProductList;
        notifyDataSetChanged();
    }

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