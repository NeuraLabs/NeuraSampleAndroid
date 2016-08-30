package com.neura.sampleapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.neura.standalonesdk.events.NeuraEvent;
import com.neura.standalonesdk.events.NeuraGCMCommandFactory;

public class NeuraEventsBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(getClass().getSimpleName(), "Received push");
        if (NeuraGCMCommandFactory.getInstance().isNeuraEvent(intent)) {
            NeuraEvent event = NeuraGCMCommandFactory.getInstance().getEvent(intent);
            String eventText = event != null ? event.toString() : "couldn't parse data";
            Log.i(getClass().getSimpleName(), "received Neura event - " + eventText);
            generateNotification(context, eventText);
        }
    }

    private void generateNotification(Context context, String eventText) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        String appName = "Neura";
        int stringId = context.getApplicationInfo().labelRes;
        if (stringId > 0)
            appName = context.getString(stringId);

        builder.setContentTitle(appName + " detected event")
                .setContentText(eventText)
                .setSmallIcon(R.drawable.neura_sdk_notification_status_icon)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), context.getApplicationInfo().icon))
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(eventText));
        Notification notification = builder.build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int) System.currentTimeMillis(), notification);
    }

}
