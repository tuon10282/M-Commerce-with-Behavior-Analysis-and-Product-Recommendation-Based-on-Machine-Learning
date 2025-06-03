package com.calmpuchia.userapp.adapters;

import android.content.Context;
import android.content.Intent;
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

        // Set price (hiển thị cả discount nếu có)
        String priceText = "₫" + product.getPrice();
        if (product.getDiscount_price() > 0 && product.getDiscount_price() < product.getPrice()) {
            priceText += " (₫" + product.getDiscount_price() + ")";
        }
        holder.productPrice.setText(priceText);

        // Load image with Picasso
        if (product.getImage_url() != null && !product.getImage_url().isEmpty()) {
            Picasso.get().load(product.getImage_url()).into(holder.productImage);
        } else {
            holder.productImage.setImageResource(R.drawable.ic_placeholder); // default image
        }
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailedProductActivity.class);
            intent.putExtra("pid", product.getProduct_id());
            intent.putExtra("name", product.getName());
            intent.putExtra("price", product.getPrice());
            intent.putExtra("image", product.getImage_url());
            intent.putStringArrayListExtra("description", new ArrayList<>(product.getDescription()));

            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice;
        ImageView productImage;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productImage = itemView.findViewById(R.id.productImage);
        }
    }
}
