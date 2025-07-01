package com.calmpuchia.userapp.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.calmpuchia.userapp.R;
import com.calmpuchia.userapp.models.Orders;

import java.text.DecimalFormat;
import java.util.List;

public class DeliveredOrderAdapter extends RecyclerView.Adapter<DeliveredOrderAdapter.ViewHolder> {

    private List<Orders> orders;
    private OnOrderClickListener listener;
    private DecimalFormat currencyFormat;

    public interface OnOrderClickListener {
        void onOrderClick(Orders order);
    }

    public DeliveredOrderAdapter(List<Orders> orders, OnOrderClickListener listener) {
        this.orders = orders;
        this.listener = listener;
        this.currencyFormat = new DecimalFormat("#,###");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_delivered_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Orders order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtOrderId, txtOrderDate, txtCustomerName, txtTotal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtOrderId = itemView.findViewById(R.id.txt_order_id);
            txtOrderDate = itemView.findViewById(R.id.txt_order_date);
            txtCustomerName = itemView.findViewById(R.id.txt_customer_name);
            txtTotal = itemView.findViewById(R.id.txt_total);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onOrderClick(orders.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Orders order) {
            txtOrderId.setText("Mã đơn: " + order.getOrder_id());
            txtOrderDate.setText("Ngày: " + order.getCreated_at());
            txtCustomerName.setText("Khách hàng: " + order.getCustomer_name());
            txtTotal.setText("Tổng tiền: " + currencyFormat.format(order.getTotal()) + "đ");
        }
    }
}

