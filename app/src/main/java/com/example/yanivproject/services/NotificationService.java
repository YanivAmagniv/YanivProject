package com.example.yanivproject.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.yanivproject.R;
import com.example.yanivproject.models.Group;
import com.example.yanivproject.models.User;
import com.example.yanivproject.models.UserPay;
import com.example.yanivproject.screens.GroupDetailsActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class NotificationService {
    private static final String CHANNEL_ID = "payment_reminders";
    private static final String TAG = "NotificationService";
    private final Context context;
    private final DatabaseReference databaseReference;

    public NotificationService(Context context) {
        this.context = context;
        this.databaseReference = FirebaseDatabase.getInstance().getReference();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Payment Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications for payment reminders and updates");
            
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void sendPaymentReminder(Group group) {
        if (group == null || group.getUserPayListAsList() == null) return;

        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        // Check if we should send a reminder based on the interval
        if (shouldSendReminder(group, currentDate)) {
            // Get all users who haven't paid yet
            group.getUserPayListAsList().stream()
                .filter(userPay -> !userPay.isPaid())
                .collect(Collectors.toList())
                .forEach(userPay -> {
                    String title = "תזכורת לתשלום";
                    String message = String.format("לא לשכוח לשלם את החלק שלך בקבוצה %s", group.getGroupName());
                    sendNotification(userPay.getUser().getId(), title, message);
                });

            // Update last reminder date
            updateLastReminderDate(group.getGroupId(), currentDate);
        }
    }

    private boolean shouldSendReminder(Group group, String currentDate) {
        if (group.getLastReminderDate() == null) return true;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date lastReminder = sdf.parse(group.getLastReminderDate());
            Date today = sdf.parse(currentDate);

            if (lastReminder != null && today != null) {
                long diffInMillis = today.getTime() - lastReminder.getTime();
                long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);
                return diffInDays >= group.getReminderInterval();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error calculating reminder interval", e);
        }
        return false;
    }

    private void createPaymentReminderNotification(Group group, UserPay userPay) {
        Intent intent = new Intent(context, GroupDetailsActivity.class);
        intent.putExtra("group", group);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            group.getGroupId().hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.smartsplitlogo)
            .setContentTitle("תזכורת לתשלום - " + group.getGroupName())
            .setContentText("סכום לתשלום: ₪" + String.format("%.2f", userPay.getAmount()))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager != null) {
            notificationManager.notify(userPay.getUser().getId().hashCode(), builder.build());
        }
    }

    private void updateLastReminderDate(String groupId, String currentDate) {
        databaseReference.child("groups")
            .child(groupId)
            .child("lastReminderDate")
            .setValue(currentDate)
            .addOnFailureListener(e -> Log.e(TAG, "Error updating last reminder date", e));
    }

    public void sendPaymentCompleteNotification(Group group, UserPay userPay) {
        Intent intent = new Intent(context, GroupDetailsActivity.class);
        intent.putExtra("group", group);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            group.getGroupId().hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.smartsplitlogo)
            .setContentTitle("תשלום הושלם - " + group.getGroupName())
            .setContentText(userPay.getUser().getName() + " שילם את חלקו")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager != null) {
            notificationManager.notify(group.getGroupId().hashCode(), builder.build());
        }
    }

    public void sendPaymentPendingNotification(Group group, UserPay userPay) {
        String title = "בקשת אישור תשלום";
        String message = String.format("%s מבקש אישור לתשלום בסך ₪%.2f בקבוצה %s",
                userPay.getUser().getName(),
                userPay.getAmount(),
                group.getGroupName());

        // Send notification to group creator
        sendNotification(group.getCreator().getId(), title, message);
    }

    private void sendNotification(String userId, String title, String message) {
        Intent intent = new Intent(context, GroupDetailsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            userId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.smartsplitlogo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager != null) {
            notificationManager.notify(userId.hashCode(), builder.build());
        }
    }

    public void sendGroupDeletedNotification(Group group, User user) {
        String title = "קבוצה נמחקה";
        String message = String.format("הקבוצה '%s' נמחקה על ידי מנהל הקבוצה", group.getGroupName());

        // Send notification to the specified user
        sendNotification(user.getId(), title, message);
    }
} 