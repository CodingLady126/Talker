package com.islavstan.free_talker.gcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.google.android.gms.gcm.GcmListenerService;
import com.islavstan.free_talker.App;
import com.islavstan.free_talker.R;
import com.islavstan.free_talker.call_functions.service.CallService;
import com.islavstan.free_talker.db.DBHelper;
import com.islavstan.free_talker.utils.PreferenceHelper;
import com.quickblox.users.model.QBUser;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class GcmPushListenerService extends GcmListenerService {
    private static final String TAG = "stas";
    //PreferenceHelper preferenceHelper;
    NotificationManager mNotificationManager;
    // QBUser qbUser;
    DBHelper dbHelper;

    @Override
    public void onMessageReceived(String from, Bundle data) {


    }


    private void showNotification() {
        Log.d(TAG, "startLoginService");
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = createNotification();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(100, notification);
        removeNotification(100);

    }


    private void removeNotification(int id) {
        Handler handler = new Handler(Looper.getMainLooper());
        long delayInMilliseconds = 20000;
        handler.postDelayed(new Runnable() {
            public void run() {
                mNotificationManager.cancel(id);
            }
        }, delayInMilliseconds);
    }


    private Notification createNotification() {
        Intent takeIntent = new Intent(this, CallReceiver.class);
        takeIntent.putExtra("action", "Take");
        PendingIntent pIntent = PendingIntent.getBroadcast(this, (int) System.currentTimeMillis(), takeIntent, 0);
        Intent rejectIntent = new Intent(this, CallReceiver.class);
        rejectIntent.putExtra("action", "Reject");
        PendingIntent pIntent2 = PendingIntent.getBroadcast(this, (int) System.currentTimeMillis(), rejectIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("IELTS Live Speaking")
                .setContentText("Incoming call")
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setDefaults(Notification.DEFAULT_ALL)
                .addAction(R.drawable.call_phone_answer_black, "Show", pIntent)
                .addAction(R.drawable.cancel, "Cancel", pIntent2)
                .setOngoing(true);

        return builder.build();
    }


}