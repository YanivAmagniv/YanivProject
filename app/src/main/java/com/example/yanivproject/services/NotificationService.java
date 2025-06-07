// NotificationService.java
// This service handles all notification-related functionality in the app
// It manages payment reminders, payment status updates, and group notifications
// Supports both local notifications and Firebase Cloud Messaging

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

/**
 * Service class for handling all notification-related functionality
 * Manages both local notifications and Firebase Cloud Messaging
 */
public class NotificationService {
    // Constants for notification channel and logging
    private static final String CHANNEL_ID = "payment_reminders";
    private static final String TAG = "NotificationService";
    
    // Context and database reference
    private final Context context;
    private final DatabaseReference databaseReference;

    /**
     * Constructor initializes the service and creates notification channel
     * @param context Application context
     */
    public NotificationService(Context context) {
        this.context = context;
        this.databaseReference = FirebaseDatabase.getInstance().getReference();
        createNotificationChannel();
    }

    /**
     * Creates the notification channel for Android O and above
     * Required for showing notifications on modern Android versions
     */
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

    /**
     * Sends payment reminders to all unpaid users in a group
     * @param group The group containing unpaid users
     */
    public void sendPaymentReminder(Group group) {
        if (group == null || group.getUserPayListAsList() == null) {
            Log.e(TAG, "Group or userPayList is null");
            return;
        }

        // Check if we should send a reminder based on the last reminder date
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        if (!shouldSendReminder(group, currentDate)) {
            Log.d(TAG, "Skipping reminder - too soon since last reminder");
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
        updateLastReminderDate(group.getGroupId(), currentDate);
    }

    /**
     * Checks if a reminder should be sent based on the last reminder date
     * @param group The group to check
     * @param currentDate Current date in yyyy-MM-dd format
     * @return true if a reminder should be sent, false otherwise
     */
    private boolean shouldSendReminder(Group group, String currentDate) {
        if (group.getLastReminderDate() == null) return true;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date lastReminder = sdf.parse(group.getLastReminderDate());
            Date today = sdf.parse(currentDate);

            if (lastReminder != null && today != null) {
                long diffInMillis = today.getTime() - lastReminder.getTime();
                long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);
                // Only send reminder if at least 1 day has passed since last reminder
                return diffInDays >= 1;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error calculating reminder interval", e);
        }
        return false;
    }

    /**
     * Creates a payment reminder notification for a specific user
     * @param group The group containing the payment
     * @param userPay The payment details
     */
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

    /**
     * Updates the last reminder date for a group
     * @param groupId The ID of the group
     * @param currentDate Current date in yyyy-MM-dd format
     */
    private void updateLastReminderDate(String groupId, String currentDate) {
        databaseReference.child("groups")
            .child(groupId)
            .child("lastReminderDate")
            .setValue(currentDate)
            .addOnFailureListener(e -> Log.e(TAG, "Error updating last reminder date", e));
    }

    /**
     * Sends a notification when a payment is completed
     * @param group The group containing the payment
     * @param userPay The completed payment details
     */
    public void sendPaymentCompleteNotification(Group group, UserPay userPay) {
        String title = "תשלום אושר";
        String message = String.format("התשלום שלך בסך ₪%.2f בקבוצה %s אושר",
                userPay.getAmount(),
                group.getGroupName());

        String memberId = userPay.getUser().getId();
        Log.d(TAG, "Sending payment complete notification to member: " + memberId);
        sendNotification(memberId, title, message);
    }

    /**
     * Sends a notification when a payment needs approval
     * @param group The group containing the payment
     * @param userPay The pending payment details
     */
    public void sendPaymentPendingNotification(Group group, UserPay userPay) {
        String title = "בקשת אישור תשלום";
        String message = String.format("%s מבקש אישור לתשלום בסך ₪%.2f בקבוצה %s",
                userPay.getUser().getName(),
                userPay.getAmount(),
                group.getGroupName());

        String creatorId = group.getCreator().getId();
        Log.d(TAG, "Sending payment pending notification to creator: " + creatorId);
        sendNotification(creatorId, title, message);
    }

    /**
     * Sends a notification to a specific user
     * @param userId The ID of the user to notify
     * @param title The notification title
     * @param message The notification message
     */
    private void sendNotification(String userId, String title, String message) {
        Log.d(TAG, "Attempting to send notification to user: " + userId);
        
        HashMap<String, Object> notification = new HashMap<>();
        notification.put("title", title);
        notification.put("message", message);
        notification.put("timestamp", String.valueOf(System.currentTimeMillis()));
        notification.put("seen", false);  // Add seen flag

        databaseReference.child("notifications")
            .child(userId)
            .push()
            .setValue(notification)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Notification sent successfully to user: " + userId);
                String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                if (userId.equals(currentUserId)) {
                    showLocalNotification(title, message);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error sending notification to user: " + userId, e);
            });
    }

    /**
     * Shows a local notification on the device
     * @param title The notification title
     * @param message The notification message
     */
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
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context).notify(
                (int) System.currentTimeMillis(),
                builder.build()
            );
        } catch (SecurityException e) {
            Log.e(TAG, "Error showing notification", e);
        }
    }

    /**
     * Sends a notification when a group is deleted
     * @param group The group that was deleted
     * @param user The user who deleted the group
     */
    public void sendGroupDeletedNotification(Group group, User user) {
        String title = "קבוצה נמחקה";
        String message = String.format("הקבוצה %s נמחקה על ידי %s",
                group.getGroupName(),
                user.getName());

        // Notify all group members except the creator
        group.getUserPayListAsList().stream()
            .filter(userPay -> !userPay.getUser().getId().equals(user.getId()))
            .forEach(userPay -> sendNotification(userPay.getUser().getId(), title, message));
    }

    /**
     * Sends a notification when a payment deadline is approaching
     * @param group The group with the approaching deadline
     * @param daysUntilDeadline Number of days until the deadline
     */
    public void sendDeadlineApproachingNotification(Group group, int daysUntilDeadline) {
        String title = "דדליין מתקרב";
        String message = String.format("נשארו %d ימים עד לתשלום בקבוצה %s",
                daysUntilDeadline,
                group.getGroupName());

        // Notify all unpaid users
        group.getUserPayListAsList().stream()
            .filter(userPay -> !userPay.isPaid())
            .forEach(userPay -> sendNotification(userPay.getUser().getId(), title, message));
    }

    /**
     * Sends a notification when all payments in a group are completed
     * @param group The group where all payments are completed
     */
    public void sendGroupPaidNotification(Group group) {
        String title = "כל התשלומים הושלמו";
        String message = String.format("כל התשלומים בקבוצה %s הושלמו בהצלחה",
                group.getGroupName());

        // Notify the group creator
        sendNotification(group.getCreator().getId(), title, message);
    }

    /**
     * Sends a payment reminder notification for a specific user
     * @param group The group containing the payment
     * @param userPay The payment that needs a reminder
     */
    public void sendPaymentReminderNotification(Group group, UserPay userPay) {
        String title = "תזכורת לתשלום";
        String message = String.format("לא לשכוח לשלם את החלק שלך בסך ₪%.2f בקבוצה %s",
                userPay.getAmount(),
                group.getGroupName());

        sendNotification(userPay.getUser().getId(), title, message);
    }

    /**
     * Marks a notification as seen
     * @param userId The ID of the user
     * @param notificationId The ID of the notification to mark as seen
     */
    public void markNotificationAsSeen(String userId, String notificationId) {
        databaseReference.child("notifications")
            .child(userId)
            .child(notificationId)
            .child("seen")
            .setValue(true)
            .addOnFailureListener(e -> Log.e(TAG, "Error marking notification as seen", e));
    }
} 