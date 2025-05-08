package com.aketsuky.aymusic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MediaButtonIntentReceiverNotification extends BroadcastReceiver {

    public MediaButtonIntentReceiverNotification() {

    }

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
    }
}
