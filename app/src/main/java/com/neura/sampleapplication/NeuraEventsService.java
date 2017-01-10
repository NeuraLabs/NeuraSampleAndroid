package com.neura.sampleapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.neura.standalonesdk.events.NeuraEvent;
import com.neura.standalonesdk.events.NeuraPushCommandFactory;

import java.util.Map;

public class NeuraEventsService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage message) {
        Map data = message.getData();
        Log.i(getClass().getSimpleName(), "Received push");
        if (NeuraPushCommandFactory.getInstance().isNeuraEvent(data)) {
            NeuraEvent event = NeuraPushCommandFactory.getInstance().getEvent(data);
            String eventText = event != null ? event.toString() : "couldn't parse data";
            Log.i(getClass().getSimpleName(), "received Neura event - " + eventText);
            generateNotification(getApplicationContext(), eventText);
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
