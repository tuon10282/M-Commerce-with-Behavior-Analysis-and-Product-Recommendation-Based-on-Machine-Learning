package com.calmpuchia.userapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.calmpuchia.userapp.R;
import com.calmpuchia.userapp.models.CartItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder> {

    private List<CartItem> cartItems;
    private Context context;
    private NumberFormat currencyFormat;

    public CheckoutAdapter(List<CartItem> cartItems) {
        this.cartItems = cartItems;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public CheckoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_checkout, parent, false);
        return new CheckoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    public class CheckoutViewHolder extends RecyclerView.ViewHolder {

        private ImageView ivProductImage;
        private TextView tvProductName;
        private TextView tvProductPrice;
        private TextView tvProductQuantity;
        private TextView tvProductTotal;
        private TextView tvProductId;

        public CheckoutViewHolder(@NonNull View itemView) {
            super(itemView);
            initViews();
        }

        private void initViews() {
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductQuantity = itemView.findViewById(R.id.tvProductQuantity);
            tvProductTotal = itemView.findViewById(R.id.tvProductTotal);
            tvProductId = itemView.findViewById(R.id.tvProductId);
        }

        public void bind(CartItem item) {
            // Set product name
            tvProductName.setText(item.getName() != null ? item.getName() : "Sản phẩm");

            // Set product ID
            if (tvProductId != null) {
                tvProductId.setText("ID: " + (item.getProductID() != null ? item.getProductID() : "N/A"));
            }

            // Set product price
            String priceText = String.format(Locale.getDefault(), "%,d đ", item.getPrice());
            tvProductPrice.setText(priceText);

            // Set quantity
            tvProductQuantity.setText("x" + item.getQuantity());

            // Calculate and set total price
            int totalPrice = item.getPrice() * item.getQuantity();
            String totalText = String.format(Locale.getDefault(), "%,d đ", totalPrice);
            tvProductTotal.setText(totalText);

            // Load product image
            loadProductImage(item);
        }

        private void loadProductImage(CartItem item) {
            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                // Load image with Glide
                RequestOptions options = new RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.ic_placeholder) // Placeholder image
                        .transform(new RoundedCorners(16));

                Glide.with(context)
                        .load(item.getImageUrl())
                        .apply(options)
                        .into(ivProductImage);
            } else {
                // Set default image if no image URL
                ivProductImage.setImageResource(R.drawable.ic_placeholder);
            }
        }
    }

    // Helper methods
    public void updateItems(List<CartItem> newItems) {
        this.cartItems = newItems;
        notifyDataSetChanged();
    }

    public void addItem(CartItem item) {
        if (cartItems != null) {
            cartItems.add(item);
            notifyItemInserted(cartItems.size() - 1);
        }
    }

    public void removeItem(int position) {
        if (cartItems != null && position >= 0 && position < cartItems.size()) {
            cartItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clearItems() {
        if (cartItems != null) {
            int size = cartItems.size();
            cartItems.clear();
            notifyItemRangeRemoved(0, size);
        }
    }

    public int getTotalPrice() {
        int total = 0;
        if (cartItems != null) {
            for (CartItem item : cartItems) {
                total += item.getPrice() * item.getQuantity();
            }
        }
        return total;
    }

    public int getTotalItems() {
        int total = 0;
        if (cartItems != null) {
            for (CartItem item : cartItems) {
                total += item.getQuantity();
            }
        }
        return total;
    }

    // Getters
    public List<CartItem> getCartItems() {
        return cartItems;
    }
}