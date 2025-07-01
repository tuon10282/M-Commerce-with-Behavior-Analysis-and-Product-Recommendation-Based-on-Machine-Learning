package com.calmpuchia.userapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.calmpuchia.userapp.R;
import com.calmpuchia.userapp.models.Products;
import com.calmpuchia.userapp.DetailedProductActivity;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductCardAdapter extends RecyclerView.Adapter<ProductCardAdapter.ProductViewHolder> {

    private Context context;
    private List<Products> productList;

    public ProductCardAdapter(Context context, List<Products> list) {
        this.context = context;
        this.productList = list != null ? list : new ArrayList<>();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, txtCurrentPrice, txtOriginalPrice, txtDiscountBadge, txtStockStatus;

        public ProductViewHolder(View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            txtCurrentPrice = itemView.findViewById(R.id.txtCurrentPrice);
            txtOriginalPrice = itemView.findViewById(R.id.txtOriginalPrice);
            txtDiscountBadge = itemView.findViewById(R.id.txtDiscountBadge);
            txtStockStatus = itemView.findViewById(R.id.txtStockStatus);
        }
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_card, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        try {
            Products product = productList.get(position);
            if (product == null) return;

            // Tên sản phẩm
            holder.tvProductName.setText(product.getName() != null ? product.getName() : "Tên sản phẩm");

            // Giá sản phẩm
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            if (product.hasDiscount()) {
                String discountPrice = formatter.format(product.getDiscount_price()) + " đ";
                String originalPrice = formatter.format(product.getPrice()) + " đ";

                holder.txtCurrentPrice.setText(discountPrice);
                holder.txtCurrentPrice.setTextColor(context.getResources().getColor(R.color.price_discount));

                holder.txtOriginalPrice.setText(originalPrice);
                holder.txtOriginalPrice.setPaintFlags(holder.txtOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                holder.txtOriginalPrice.setVisibility(View.VISIBLE);

                holder.txtDiscountBadge.setVisibility(View.VISIBLE);
                holder.txtDiscountBadge.setText("-" + (int) product.getDiscountPercentage() + "%");
            } else {
                if (product.getPrice() > 0) {
                    String formattedPrice = formatter.format(product.getPrice()) + " đ";
                    holder.txtCurrentPrice.setText(formattedPrice);
                    holder.txtCurrentPrice.setTextColor(context.getResources().getColor(R.color.text_primary));
                } else {
                    holder.txtCurrentPrice.setText("Giá: Liên hệ");
                }

                holder.txtOriginalPrice.setVisibility(View.GONE);
                holder.txtDiscountBadge.setVisibility(View.GONE);
            }

            // Tồn kho
            if (product.getStock() <= 0) {
                holder.txtStockStatus.setText("Hết hàng");
                holder.txtStockStatus.setVisibility(View.VISIBLE);
                holder.itemView.setAlpha(0.6f);
            } else if (product.getStock() <= 5) {
                holder.txtStockStatus.setText("Còn " + product.getStock());
                holder.txtStockStatus.setVisibility(View.VISIBLE);
                holder.itemView.setAlpha(1.0f);
            } else {
                holder.txtStockStatus.setVisibility(View.GONE);
                holder.itemView.setAlpha(1.0f);
            }

            // Hình ảnh
            if (!TextUtils.isEmpty(product.getImage_url())) {
                Glide.with(context)
                        .load(product.getImage_url())
                        .into(holder.ivProductImage);
            } else {
                holder.ivProductImage.setImageResource(R.mipmap.ic_empty_box);
            }

            // Click để mở chi tiết
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, DetailedProductActivity.class);
                intent.putExtra("pid", product.getProduct_id());
                intent.putExtra("name", product.getName());
                intent.putExtra("price", product.getPrice());
                intent.putExtra("image", product.getImage_url());
                if (product.getDescription() != null) {
                    intent.putStringArrayListExtra("description", new ArrayList<>(product.getDescription()));
                }
                context.startActivity(intent);
            });

        } catch (Exception e) {
            Log.e("ProductCardAdapter", "Lỗi khi binding sản phẩm: " + e.getMessage(), e);
        }
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public void updateProducts(List<Products> newProducts) {
        productList.clear();
        if (newProducts != null) {
            productList.addAll(newProducts);
        }
        notifyDataSetChanged();
    }

    public void addProduct(Products product) {
        if (product != null) {
            productList.add(product);
            notifyItemInserted(productList.size() - 1);
        }
    }

    public void removeProduct(int position) {
        if (position >= 0 && position < productList.size()) {
            productList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clearProducts() {
        productList.clear();
        notifyDataSetChanged();
    }
}