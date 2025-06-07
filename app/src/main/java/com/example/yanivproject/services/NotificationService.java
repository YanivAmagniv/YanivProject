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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
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
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for payment reminders and updates");
            
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void sendPaymentReminder(Group group) {
        if (group == null || group.getUserPayListAsList() == null) {
            Log.e(TAG, "Group or userPayList is null");
            return;
        }

        Log.d(TAG, "Getting unpaid users for immediate reminder");
        // Get all users who haven't paid yet, excluding the creator
        List<UserPay> unpaidUsers = group.getUserPayListAsList().stream()
            .filter(userPay -> !userPay.isPaid() && !userPay.getUser().getId().equals(group.getCreator().getId()))
            .collect(Collectors.toList());

        Log.d(TAG, "Found " + unpaidUsers.size() + " unpaid users to notify");
        
        unpaidUsers.forEach(userPay -> {
            String title = "תזכורת לתשלום";
            String message = String.format("לא לשכוח לשלם את החלק שלך בסך ₪%.2f בקבוצה %s", 
                userPay.getAmount(), 
                group.getGroupName());
            Log.d(TAG, "Preparing to send reminder to user: " + userPay.getUser().getId());
            sendNotification(userPay.getUser().getId(), title, message);
        });

        // Update last reminder date
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        updateLastReminderDate(group.getGroupId(), currentDate);
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
        String title = "תשלום אושר";
        String message = String.format("התשלום שלך בסך ₪%.2f בקבוצה %s אושר",
                userPay.getAmount(),
                group.getGroupName());

        // Send notification only to the member who made the payment
        String memberId = userPay.getUser().getId();
        Log.d(TAG, "Sending payment complete notification to member: " + memberId);
        sendNotification(memberId, title, message);
    }

    public void sendPaymentPendingNotification(Group group, UserPay userPay) {
        String title = "בקשת אישור תשלום";
        String message = String.format("%s מבקש אישור לתשלום בסך ₪%.2f בקבוצה %s",
                userPay.getUser().getName(),
                userPay.getAmount(),
                group.getGroupName());

        // Send notification only to group creator
        String creatorId = group.getCreator().getId();
        Log.d(TAG, "Sending payment pending notification to creator: " + creatorId);
        sendNotification(creatorId, title, message);
    }

    private void sendNotification(String userId, String title, String message) {
        Log.d(TAG, "Attempting to send notification to user: " + userId);
        
        // Create a notification in the database
        HashMap<String, String> notification = new HashMap<>();
        notification.put("title", title);
        notification.put("message", message);
        notification.put("timestamp", String.valueOf(System.currentTimeMillis()));

        databaseReference.child("notifications")
            .child(userId)
            .push()
            .setValue(notification)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Notification sent successfully to user: " + userId);
                // Only show local notification if this is the current user
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                if (userId.equals(currentUserId)) {
                    showLocalNotification(title, message);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error sending notification to user: " + userId, e);
            });
    }

    public void showLocalNotification(String title, String message) {
        Intent intent = new Intent(context, GroupDetailsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.smartsplitlogo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    public void sendGroupDeletedNotification(Group group, User user) {
        String title = "קבוצה נמחקה";
        String message = String.format("הקבוצה '%s' נמחקה על ידי מנהל הקבוצה", group.getGroupName());

        // Send notification to the specified user (who is not the creator)
        Log.d(TAG, "Sending group deleted notification to user: " + user.getId());
        sendNotification(user.getId(), title, message);
    }
} 