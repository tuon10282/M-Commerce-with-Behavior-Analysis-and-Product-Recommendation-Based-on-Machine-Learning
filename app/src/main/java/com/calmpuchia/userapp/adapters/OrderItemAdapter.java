package com.calmpuchia.userapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.calmpuchia.userapp.R;
import com.calmpuchia.userapp.models.OrderItem;

import java.text.DecimalFormat;
import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ViewHolder> {

    private List<OrderItem> items;
    private OnItemClickListener listener;
    private DecimalFormat currencyFormat;

    public interface OnItemClickListener {
        void onItemClick(OrderItem item);
    }

    public OrderItemAdapter(List<OrderItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
        this.currencyFormat = new DecimalFormat("#,###");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgProduct;
        private TextView txtProductName, txtQuantity, txtPrice;
        private LinearLayout btnReview; // Change from Button to LinearLayout
        private TextView txtReviewText; // Add reference to the TextView inside

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_product);
            txtProductName = itemView.findViewById(R.id.txt_product_name);
            txtQuantity = itemView.findViewById(R.id.txt_quantity);
            txtPrice = itemView.findViewById(R.id.txt_price);
            btnReview = itemView.findViewById(R.id.btn_review); // Now works correctly
            txtReviewText = btnReview.findViewById(R.id.txt_review); // Get the TextView inside

            btnReview.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(items.get(getAdapterPosition()));
                }
            });
        }

        public void bind(OrderItem item) {
            txtProductName.setText(item.getProduct_name());
            txtQuantity.setText("Số lượng: " + item.getQuantity());
            txtPrice.setText(currencyFormat.format(item.getTotalPrice()) + "đ");

            // Load product image
            if (item.getProduct_image() != null && !item.getProduct_image().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(item.getProduct_image())
                        .into(imgProduct);
            }

            // Update button state
            if (item.isReviewed()) {
                txtReviewText.setText("Đã đánh giá");
                btnReview.setEnabled(false);
                btnReview.setBackgroundColor(itemView.getContext().getColor(R.color.gray));
            } else {
                txtReviewText.setText("Đánh giá");
                btnReview.setEnabled(true);
                btnReview.setBackgroundColor(itemView.getContext().getColor(R.color.primary));
            }
        }
    }}