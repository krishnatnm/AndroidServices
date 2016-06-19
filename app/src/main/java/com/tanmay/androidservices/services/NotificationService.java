package com.tanmay.androidservices.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.tanmay.androidservices.R;
import com.tanmay.androidservices.views.activities.ManageAlarms;

/**
 * Created by TaNMay on 19/06/16.
 */
public class NotificationService extends Service {

//        extends IntentService {

    private static final int NOTIFICATION_ID = 1234;
    public static String TAG = "Notification ==>";
    Context context;

//    public NotificationService() {
//        super("NotificationService");
//    }
//
//    @Override
//    protected void onHandleIntent(Intent intent) {
//        Log.d(getClass().getSimpleName(), "I ran!");
//        context = this;
//        String content = intent.getStringExtra("CONTENT");
//
//        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        Intent notificationIntent = new Intent(context, ManageAlarms.class);
////        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(context)
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setContentTitle("Alarm")
//                .setContentText(content + "!")
//                .setSound(alarmSound)
//                .setAutoCancel(true)
//                .setContentIntent(pendingIntent)
//                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
//        notificationManager.notify(NOTIFICATION_ID, mNotifyBuilder.build());
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println(TAG + " On Create!");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println(TAG + " On Start Command!");

        context = this;
        String content = intent.getStringExtra("CONTENT");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(context, ManageAlarms.class);
//        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Alarm")
                .setContentText(content + "!")
                .setSound(alarmSound)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
        notificationManager.notify(NOTIFICATION_ID, mNotifyBuilder.build());
        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
