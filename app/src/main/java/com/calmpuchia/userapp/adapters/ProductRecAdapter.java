package com.calmpuchia.userapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.calmpuchia.userapp.R;
import com.calmpuchia.userapp.models.ProductsRec;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductRecAdapter extends RecyclerView.Adapter<ProductRecAdapter.ViewHolder> {
    private Context context;
    private List<ProductsRec> productList;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(ProductsRec product);
    }

    public ProductRecAdapter(Context context, List<ProductsRec> productList) {
        this.context = context;
        this.productList = productList;
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }

    public void updateProductList(List<ProductsRec> newProductList) {
        this.productList = newProductList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_homepage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductsRec product = productList.get(position);

        // Set product name
        if (product.getName() != null) {
            holder.tvProductName.setText(product.getName());
        } else {
            holder.tvProductName.setText("Unknown Product");
        }

        // Set product price with Vietnamese format
        double price = product.getPrice();
        String formattedPrice = formatPrice(price);
        holder.tvProductPrice.setText(formattedPrice);

        // Show loading overlay initially
        holder.imageLoadingOverlay.setVisibility(View.VISIBLE);

        // Set product image using Glide
        if (product.getImage_url() != null && !product.getImage_url().isEmpty()) {
            Glide.with(context)
                    .load(product.getImage_url())
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                            holder.imageLoadingOverlay.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                            holder.imageLoadingOverlay.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(holder.ivProductImage);
        } else {
            holder.ivProductImage.setImageResource(R.mipmap.ic_launcher);
            holder.imageLoadingOverlay.setVisibility(View.GONE);
        }

        // Handle click events
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });
    }

    private String formatPrice(double price) {
        // Format price with Vietnamese currency
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return "₫" + formatter.format((long) price);
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName;
        TextView tvProductPrice;
        View imageLoadingOverlay;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.productImage);
            tvProductName = itemView.findViewById(R.id.productName);
            tvProductPrice = itemView.findViewById(R.id.productPrice);
            imageLoadingOverlay = itemView.findViewById(R.id.imageLoadingOverlay);
        }
    }
}