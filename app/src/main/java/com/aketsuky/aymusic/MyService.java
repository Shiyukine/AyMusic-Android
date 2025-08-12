package com.aketsuky.aymusic;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;

public class MyService extends Service {
    WebView wb;
    public static MyService instance = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        wb = MainActivity.actualWb;
        instance = this;
        if(WebAppInterface.aNoti != null)
            startForeground(1, WebAppInterface.aNoti);
        return super.onStartCommand(intent, flags, startId);
    }

    @OptIn(markerClass = UnstableApi.class)
    public void updateNotif() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, WebAppInterface.aNoti);
    }

    /*@Override
    public void onCreate() {
        super.onCreate();
        Log.e("fsdqfdsqfs", "dsqffsdqf");
        wb = MainActivity.actualWb;
        startForeground(1, WebAppInterface.aNoti);
    }*/

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
