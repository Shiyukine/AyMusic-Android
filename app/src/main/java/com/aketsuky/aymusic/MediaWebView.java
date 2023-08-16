package com.aketsuky.aymusic;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

public class MediaWebView extends WebView {

    public MediaWebView(Context context) {
        super(context);
    }

    public MediaWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MediaWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(View.VISIBLE);
    }
}