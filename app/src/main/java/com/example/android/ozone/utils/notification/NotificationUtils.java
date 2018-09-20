package com.example.android.ozone.utils.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.example.android.ozone.R;
import com.example.android.ozone.ui.view.LocationFragment;

//Inspired from Udacity course on Notifications
public class NotificationUtils {

    private static final int OZONE_PENDING_INTENT_ID = 1234;
    private static final int OZONE_NOTIFICATION_ID = 12345;
    private static final String OZONE_NOTIFICATION_CHANEL = "ozone_notification_chanel";

    public static void showNotificationAfterUpdate(Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(OZONE_NOTIFICATION_CHANEL,
                    "Ozone", NotificationManager.IMPORTANCE_HIGH);

            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, OZONE_NOTIFICATION_CHANEL)
                        .setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                        .setSmallIcon(R.drawable.ic_face)
                        .setLargeIcon(largeIcon(context))
                        .setContentTitle(context.getString(R.string.ozone_notification))
                        .setContentText(context.getString(R.string.data_updated))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(context.getString(R.string.data_updated)))
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentIntent(contentIntent(context))
                        .setPriority(NotificationManager.IMPORTANCE_HIGH)
                        .setAutoCancel(true);

        notificationManager.notify(OZONE_NOTIFICATION_ID, notificationBuilder.build());


    }

    private static PendingIntent contentIntent(Context context) {
        Intent intent = new Intent(context, LocationFragment.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                OZONE_PENDING_INTENT_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    private static Bitmap largeIcon(Context context) {
        Resources resources = context.getResources();
        Bitmap icon = BitmapFactory.decodeResource(resources, R.drawable.ic_face);
        return icon;
    }
}
