package com.aketsuky.aymusic;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.webkit.WebView;

import androidx.annotation.Nullable;

public class MyService extends Service {
    WebView wb;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        wb = MainActivity.actualWb;
        startForeground(1, WebAppInterface.aNoti);
        return super.onStartCommand(intent, flags, startId);
    }

    /*@Override
    public void onCreate() {
        super.onCreate();
        Log.e("fsdqfdsqfs", "dsqffsdqf");
        wb = MainActivity.actualWb;
        startForeground(1, WebAppInterface.aNoti);
    }*/
}
