package com.aketsuky.aymusic;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;

public class MediaButtonIntentReceiverNotification extends BroadcastReceiver {

    public MediaButtonIntentReceiverNotification() {

    }

    @SuppressLint("RestrictedApi")
    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("mediaPause")) {
            WebAppInterface._mediaSession.getController().getTransportControls().pause();
        }
        if(intent.getAction().equals("mediaPlay")) {
            WebAppInterface._mediaSession.getController().getTransportControls().play();
        }
        if(intent.getAction().equals("mediaNext")) {
            WebAppInterface._mediaSession.getController().getTransportControls().skipToNext();
        }
        if(intent.getAction().equals("mediaPrevious")) {
            WebAppInterface._mediaSession.getController().getTransportControls().skipToPrevious();
        }
        if(intent.getAction().startsWith("shuffle"))
        {
            WebAppInterface._mediaSession.getController().getTransportControls().sendCustomAction(intent.getAction(), null);
        }
        if(intent.getAction().startsWith("repeat"))
        {
            WebAppInterface._mediaSession.getController().getTransportControls().sendCustomAction(intent.getAction(), null);
        }
    }
}
