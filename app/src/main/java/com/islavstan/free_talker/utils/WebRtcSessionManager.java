package com.islavstan.free_talker.utils;

import android.content.Context;
import android.nfc.Tag;
import android.util.Log;

import com.islavstan.free_talker.App;
import com.islavstan.free_talker.db.DBHelper;
import com.islavstan.free_talker.main.MainActivity;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacksImpl;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class WebRtcSessionManager extends QBRTCClientSessionCallbacksImpl {
    private static final String TAG = "stas";

    private static WebRtcSessionManager instance;
    private Context context;
    private static QBRTCSession currentSession;
    DBHelper dbHelper;

    private WebRtcSessionManager(Context context) {
        this.context = context;
        dbHelper = DBHelper.getInstance(context);
    }

    public static WebRtcSessionManager getInstance(Context context) {
        if (instance == null) {
            Log.d(TAG, "instance = new WebRtcSessionManager");
            instance = new WebRtcSessionManager(context);
        }
        return instance;
    }

    public QBRTCSession getCurrentSession() {
        return currentSession;
    }

    public void setCurrentSession(QBRTCSession qbCurrentSession) {
        currentSession = qbCurrentSession;


    }

    @Override
    public void onReceiveNewSession(QBRTCSession session) {
        Log.d(TAG, "onReceiveNewSession to WebRtcSessionManager");
        Log.d(TAG, App.isAppOpen() + " App.isAppOpen() WebRtcSessionManager");

        if (currentSession == null) {
            Log.d(TAG, "caller id = " + session.getCallerID());
            // здесь проверять заблочен ли юзер
            dbHelper.getBlockUser(session.getCallerID())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                        if (result == 0) {

                            setCurrentSession(session);
                            MainActivity.start(context, true);

                        } else {
                            Log.d(TAG, "call block user");
                        }

                    }, error -> Log.d(TAG, "getBlockUser error = " + error.getMessage()));

        }
    }


    @Override
    public void onSessionClosed(QBRTCSession session) {
        Log.d(TAG, "onSessionClosed WebRtcSessionManager");
        if (session.equals(getCurrentSession())) {
            setCurrentSession(null);
        }
    }
}