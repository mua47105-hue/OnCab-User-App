package com.oncab.user.Service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.oncab.user.MainActivity;
import com.oncab.user.R;

/** Faithful port of OrderNotification.kt (FCM MESSAGING_EVENT handler). */
public class OrderNotification extends FirebaseMessagingService {

    public static final String CHANNEL_ID = "oncab_orders";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        RemoteMessage.Notification n = message.getNotification();
        String title = n != null && n.getTitle() != null ? n.getTitle() : "OnCab";
        String body = n != null && n.getBody() != null ? n.getBody() : "You have a new update";
        showNotification(title, body);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        getSharedPreferences(ConstantsHolder.FILE_NAME, 0).edit()
                .putString(ConstantsHolder.DEVICE_ID, token).apply();
    }

    private void showNotification(String title, String body) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Order Updates",
                    NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_logo_new)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pi);
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    /** Avoids importing Utils constants in the notification path. */
    private static final class ConstantsHolder {
        static final String FILE_NAME = "USER_DETAILS";
        static final String DEVICE_ID = "device_id";
    }
}
