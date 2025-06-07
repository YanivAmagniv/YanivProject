// MyFirebaseMessagingService.java
// This service handles Firebase Cloud Messaging (FCM) notifications
// It receives and processes push notifications from Firebase
// Extends FirebaseMessagingService to handle incoming messages

package com.example.yanivproject.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.yanivproject.R;
import com.example.yanivproject.screens.GroupDetailsActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Service class for handling Firebase Cloud Messaging notifications
 * Processes incoming push notifications and displays them to the user
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    // Constants for logging and notification channel
    private static final String TAG = "MyFirebaseMsgService";
    private static final String CHANNEL_ID = "payment_reminders";

    /**
     * Called when a message is received from Firebase Cloud Messaging
     * Processes the message data and displays a notification
     * @param message The received RemoteMessage containing notification data
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        Log.d(TAG, "Message received: " + message.getData());
        
        if (message.getData().size() > 0) {
            String title = message.getData().get("title");
            String body = message.getData().get("message");
            
            if (title != null && body != null) {
                showNotification(title, body);
            }
        }
    }

    /**
     * Creates and displays a local notification
     * @param title The notification title
     * @param message The notification message
     */
    private void showNotification(String title, String message) {
        createNotificationChannel();

        // Create intent to open GroupDetailsActivity when notification is tapped
        Intent intent = new Intent(this, GroupDetailsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.smartsplitlogo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);

        // Display the notification
        NotificationManager notificationManager = 
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        }
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
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
} 