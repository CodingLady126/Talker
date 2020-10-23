package com.islavstan.free_talker;

import android.app.Application;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.quickblox.auth.session.QBSettings;
import com.quickblox.core.ServiceZone;
import io.fabric.sdk.android.Fabric;


public class App extends Application {

    public static final String APP_ID = "56925";
    public static final String AUTH_KEY = "hkQgprSY7YBSVbr";
    public static final String AUTH_SECRET = "JGPJjry9xZMsH8S";
    public static final String ACCOUNT_KEY = "ZVtq8i2UTCe26RRHtzsG";





    private static boolean appOpen = false;
    private static boolean appOnPause = false;

    public static boolean isAppOnPause() {
        return appOnPause;
    }

    public static void setAppOnPause(boolean appOnPause) {
        App.appOnPause = appOnPause;
    }

    public static boolean isAppOpen() {
        return appOpen;
    }

    public static void setAppOpen(boolean appOpen) {
        App.appOpen = appOpen;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("stas", "app onCreate");
        Fabric.with(this, new Crashlytics());
        QBSettings.getInstance().init(getApplicationContext(), APP_ID, AUTH_KEY, AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);
        QBSettings.getInstance().setEndpoints("https://api.quickblox.com", "chat.quickblox.com", ServiceZone.PRODUCTION);
        QBSettings.getInstance().setZone(ServiceZone.PRODUCTION);

    }




}
