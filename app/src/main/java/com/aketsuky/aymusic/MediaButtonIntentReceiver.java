package com.aketsuky.aymusic;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.webkit.WebView;

public class MediaButtonIntentReceiver extends BroadcastReceiver {

    WebView wb;

    MediaButtonIntentReceiver(WebView wb) {
        this.wb = wb;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        wb.requestFocus();
        if(intent.getAction().equals("mediaPause")) {
            wb.evaluateJavascript("listeners.player.pause()", null);
        }
        if(intent.getAction().equals("mediaPlay")) {
            wb.evaluateJavascript("listeners.player.play()", null);
        }
        if(intent.getAction().equals("mediaNext")) {
            wb.evaluateJavascript("listeners.player.next()", null);
        }
        if(intent.getAction().equals("mediaPrevious")) {
            wb.evaluateJavascript("listeners.player.previous()", null);
        }
    }
}
