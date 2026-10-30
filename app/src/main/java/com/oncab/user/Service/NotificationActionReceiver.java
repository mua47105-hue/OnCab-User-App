package com.oncab.user.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Faithful port of NotificationActionReceiver.kt — handles notification actions. */
public class NotificationActionReceiver extends BroadcastReceiver {

    public static final String ACTION_DISMISS = "com.oncab.user.DISMISS_NOTIFICATION";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_DISMISS.equals(intent.getAction())) {
            int id = intent.getIntExtra("notification_id", -1);
            if (id != -1) {
                android.app.NotificationManager manager =
                        (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (manager != null) {
                    manager.cancel(id);
                }
            }
        }
    }
}
