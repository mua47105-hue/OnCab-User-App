package com.oncab.user.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.oncab.user.R;
import com.oncab.user.Utils.Constants;

/** Faithful port of LocationService.kt — foreground location service (id 175). */
public class LocationService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : "";
        if (Constants.ACTION_STOP_LOCATION_SERVICE.equals(action)) {
            stopSelf();
            return START_NOT_STICKY;
        }
        startForeground(Constants.LOCATION_SERVICE_ID, buildNotification());
        return START_STICKY;
    }

    private Notification buildNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String channelId = "oncab_location";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Location Service", NotificationManager.IMPORTANCE_LOW);
            manager.createNotificationChannel(channel);
        }
        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("OnCab is tracking your ride")
                .setContentText("Location access is active for a smoother pickup experience.")
                .setSmallIcon(R.mipmap.ic_logo_new)
                .setOngoing(true)
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
