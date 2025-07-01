package com.calmpuchia.userapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.calmpuchia.userapp.R;
import com.calmpuchia.userapp.models.Products;
import com.calmpuchia.userapp.NearbyProductsActivity;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class NearbyDealsAdapter extends RecyclerView.Adapter<NearbyDealsAdapter.ViewHolder> {

    private Context context;
    private List<Products> productList;

    public NearbyDealsAdapter(Context context, List<Products> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng layout nhỏ cho homepage
        View view = LayoutInflater.from(context).inflate(R.layout.item_nearby_deal_small, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Products product = productList.get(position);
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

        // Set product name
        holder.tvProductName.setText(product.getName());

        // Display price based on whether there's a discount
        if (product.hasDiscount()) {
            // Show discounted price as current price
            String discountedPrice = formatter.format(product.getDiscount_price()) + "đ";
            holder.txtCurrentPrice.setText(discountedPrice);

            // Show original price with strikethrough
            String originalPriceText = formatter.format(product.getPrice()) + "đ";
            holder.txtOriginalPrice.setText(originalPriceText);
            holder.txtOriginalPrice.setVisibility(View.VISIBLE);

            // Add strikethrough effect
            holder.txtOriginalPrice.setPaintFlags(
                    holder.txtOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            );

            // Show discount percentage
            String discountText = "-" + Math.round(product.getDiscountPercentage()) + "%";
            holder.txtDiscountBadge.setText(discountText);
            holder.txtDiscountBadge.setVisibility(View.VISIBLE);
        } else {
            // No discount - show regular price
            if (product.getPrice() > 0) {
                String formattedPrice = formatter.format(product.getPrice()) + "đ";
                holder.txtCurrentPrice.setText(formattedPrice);
            }

            // Hide discount-related views
            holder.txtOriginalPrice.setVisibility(View.GONE);
            holder.txtDiscountBadge.setVisibility(View.GONE);
        }

        // Load product image using Glide
        if (product.getImage_url() != null && !product.getImage_url().isEmpty()) {
            Glide.with(context)
                    .load(product.getImage_url())
                    .into(holder.ivProductImage);
        } else {
            holder.ivProductImage.setImageResource(R.mipmap.ic_empty_box);
        }

        // Set click listener for product item
        holder.cardView.setOnClickListener(v -> {
            // Navigate to product detail if needed
            // Intent intent = new Intent(context, ProductDetailActivity.class);
            // intent.putExtra("product_id", product.getEffectiveProductId());
            // context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return Math.min(productList.size(), 5); // Chỉ hiển thị tối đa 5 sản phẩm
    }

    public void updateProducts(List<Products> newProducts) {
        this.productList = newProducts;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivProductImage;
        TextView tvProductName;
        TextView txtCurrentPrice;
        TextView txtOriginalPrice;
        TextView txtDiscountBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            txtCurrentPrice = itemView.findViewById(R.id.txtCurrentPrice);
            txtOriginalPrice = itemView.findViewById(R.id.txtOriginalPrice);
            txtDiscountBadge = itemView.findViewById(R.id.txtDiscountBadge);
        }
    }
}