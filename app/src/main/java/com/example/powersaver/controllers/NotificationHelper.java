package com.example.powersaver.controllers;
import android.util.Log;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.powersaver.R;

public class NotificationHelper {

    private static final String CHANNEL_ID = "power_usage_channel";
    private static final String CHANNEL_NAME = "Power Usage Alerts";
    private static final String CHANNEL_DESCRIPTION = "Notifications for high power usage";

    private final Context context;

    public NotificationHelper(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    /**
     * Creates a notification channel for Android Oreo and above.
     */
    private void createNotificationChannel() {
        // Notification channels are required for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Define the importance level of the channel
            int importance = NotificationManager.IMPORTANCE_HIGH;

            // Create the notification channel
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            channel.setDescription(CHANNEL_DESCRIPTION);

            // Register the channel with the system
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Sends a high power usage notification.
     *
     * @param title   The title of the notification.
     * @param message The message content of the notification.
     */
    public void sendHighPowerUsageNotification(String title, String message) {
        // For Android 13 and above, check for POST_NOTIFICATIONS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, do not send notification
                // Optionally, log this event
                Log.d("NotificationHelper", "POST_NOTIFICATIONS permission not granted.");
                return;
            }
        }

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_foreground) // Use the foreground icon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // For heads-up notification
                .setAutoCancel(true); // Dismiss notification when clicked

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify(getNotificationId(), builder.build());
        } catch (SecurityException e) {
            // Handle the exception if the permission is somehow not granted
            e.printStackTrace();
        }
    }

    /**
     * Generates a unique notification ID.
     *
     * @return A unique integer for the notification ID.
     */
    private int getNotificationId() {
        return (int) System.currentTimeMillis();
    }
}
