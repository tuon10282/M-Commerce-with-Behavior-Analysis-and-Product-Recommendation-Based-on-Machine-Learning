package com.calmpuchia.userapp.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.calmpuchia.userapp.R;
import com.calmpuchia.userapp.models.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private static final String TAG = "CartAdapter";
    private List<CartItem> cartItems;
    private Context context;
    private OnTotalChangeListener onTotalChangeListener;
    private FirebaseFirestore db;

    // Interface for total calculation callback
    public interface OnTotalChangeListener {
        void onTotalChanged();
    }

    public CartAdapter(List<CartItem> cartItems, OnTotalChangeListener listener) {
        this.cartItems = cartItems;
        this.onTotalChangeListener = listener;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public class CartViewHolder extends RecyclerView.ViewHolder {
        private CheckBox checkboxSelect;
        private ImageView imageProduct;
        private TextView textProductName;
        private TextView textProductPrice;
        private TextView textQuantity;
        private ImageButton btnDecrease;
        private ImageButton btnIncrease;
        private ImageButton btnRemove;
        private TextView textTotalPrice;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);

            checkboxSelect = itemView.findViewById(R.id.checkboxSelect);
            imageProduct = itemView.findViewById(R.id.imageProduct);
            textProductName = itemView.findViewById(R.id.textProductName);
            textProductPrice = itemView.findViewById(R.id.textProductPrice);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            textTotalPrice = itemView.findViewById(R.id.textTotalPrice);
        }

        public void bind(CartItem item, int position) {
            // Set product name
            textProductName.setText(item.getName());

            // Set product price (show discount if available)
            if (item.isHasDiscount() && item.getDiscountPrice() > 0) {
                textProductPrice.setText(String.format("%,d $", item.getDiscountPrice()));
            } else {
                textProductPrice.setText(String.format("%,d $", item.getPrice()));
            }

            // Set quantity
            textQuantity.setText(String.valueOf(item.getQuantity()));

            // Set total price for this item
            textTotalPrice.setText(String.format("%,d $", item.getEffectiveTotalPrice()));

            // Set checkbox state
            checkboxSelect.setChecked(item.isSelected());

            // Load product image
            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(item.getImageUrl())
                        .into(imageProduct);
            }

            // Checkbox click listener
            checkboxSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setSelected(isChecked);
                if (onTotalChangeListener != null) {
                    onTotalChangeListener.onTotalChanged();
                }
            });

            // Decrease quantity button
            btnDecrease.setOnClickListener(v -> {
                if (item.getQuantity() > 1) {
                    item.setQuantity(item.getQuantity() - 1);
                    textQuantity.setText(String.valueOf(item.getQuantity()));
                    textTotalPrice.setText(String.format("%,d $", item.getEffectiveTotalPrice()));

                    // Update in Firestore
                    updateQuantityInFirestore(item);

                    if (onTotalChangeListener != null) {
                        onTotalChangeListener.onTotalChanged();
                    }
                } else {
                    Toast.makeText(context, "Số lượng tối thiểu là 1", Toast.LENGTH_SHORT).show();
                }
            });

            // Increase quantity button
            btnIncrease.setOnClickListener(v -> {
                item.setQuantity(item.getQuantity() + 1);
                textQuantity.setText(String.valueOf(item.getQuantity()));
                textTotalPrice.setText(String.format("%,d $", item.getEffectiveTotalPrice()));

                // Update in Firestore
                updateQuantityInFirestore(item);

                if (onTotalChangeListener != null) {
                    onTotalChangeListener.onTotalChanged();
                }
            });

            // Remove item button
            btnRemove.setOnClickListener(v -> {
                removeItemFromCart(item, position);
            });
        }
    }

    private void updateQuantityInFirestore(CartItem item) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) return;

        // Find the cart document for this user
        db.collection("carts")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Calculate new total price
                        int newTotalPrice = item.getEffectivePrice() * item.getQuantity();

                        // Update the specific item in the items map
                        String itemPath = "items." + item.getProductID();

                        db.collection("carts").document(documentId)
                                .update(
                                        itemPath + ".quantity", item.getQuantity(),
                                        itemPath + ".total_price", item.getEffectivePriceAsDouble() * item.getQuantity()
                                )
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Quantity updated successfully");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error updating quantity: " + e.getMessage());
                                    Toast.makeText(context, "Lỗi cập nhật số lượng", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error finding cart: " + e.getMessage());
                });
    }

    private void removeItemFromCart(CartItem item, int position) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) return;

        // Find the cart document for this user
        db.collection("carts")
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String documentId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Remove the specific item from the items map
                        String itemPath = "items." + item.getProductID();

                        db.collection("carts").document(documentId)
                                .update(itemPath, com.google.firebase.firestore.FieldValue.delete())
                                .addOnSuccessListener(aVoid -> {
                                    // Remove from local list
                                    cartItems.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, cartItems.size());

                                    if (onTotalChangeListener != null) {
                                        onTotalChangeListener.onTotalChanged();
                                    }

                                    Toast.makeText(context, "Đã xóa sản phẩm khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "Item removed successfully");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error removing item: " + e.getMessage());
                                    Toast.makeText(context, "Lỗi xóa sản phẩm", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error finding cart to remove item: " + e.getMessage());
                });
    }
    // Method to get selected items
    public List<CartItem> getSelectedItems() {
        return cartItems.stream()
                .filter(CartItem::isSelected)
                .collect(java.util.stream.Collectors.toList());
    }

    // Method to select/deselect all items
    public void selectAllItems(boolean selectAll) {
        for (CartItem item : cartItems) {
            item.setSelected(selectAll);
        }
        notifyDataSetChanged();
        if (onTotalChangeListener != null) {
            onTotalChangeListener.onTotalChanged();
        }
    }
}