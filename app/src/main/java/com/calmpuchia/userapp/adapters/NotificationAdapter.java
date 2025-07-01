package com.calmpuchia.userapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.calmpuchia.userapp.R;
import com.calmpuchia.userapp.models.NotificationModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<NotificationModel> notifications;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(NotificationModel notification);
    }

    public NotificationAdapter(List<NotificationModel> notifications, Context context) {
        this.notifications = notifications;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationModel notification = notifications.get(position);

        holder.titleText.setText(notification.getTitle());
        holder.messageText.setText(notification.getMessage());
        holder.timeText.setText(formatTime(notification.getTimestamp()));

        // Show/hide unread indicator
        holder.unreadIndicator.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);

        // Set opacity based on read status
        holder.itemView.setAlpha(notification.isRead() ? 0.6f : 1.0f);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(notification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    private String formatTime(long timestamp) {
        Date date = new Date(timestamp);
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 60000) { // Less than 1 minute
            return "Just now";
        } else if (diff < 3600000) { // Less than 1 hour
            return (diff / 60000) + " min ago";
        } else if (diff < 86400000) { // Less than 1 day
            return (diff / 3600000) + " hour ago";
        } else {
            return new SimpleDateFormat("MMM dd", Locale.getDefault()).format(date);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, messageText, timeText;
        View unreadIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.txtNotificationTitle);
            messageText = itemView.findViewById(R.id.txtNotificationMessage);
            timeText = itemView.findViewById(R.id.txtNotificationTime);
            unreadIndicator = itemView.findViewById(R.id.unreadIndicator);
        }
    }
}