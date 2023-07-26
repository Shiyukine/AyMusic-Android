package com.aketsuky.aymusic;

import android.content.Context;
import android.content.Intent;
import android.os.Debug;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpCookie;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WebAppInterface {
    Context mContext;
    WebView view;
    MainActivity mainActivity;
    static HashMap<String, String> clientsToken = new HashMap<>();

    WebAppInterface(Context c, WebView wv, MainActivity main) {
        mContext = c;
        view = wv;
        mainActivity = main;
    }

    @JavascriptInterface
    public String getSettingFile() {
        return "{}";
    }

    @JavascriptInterface
    public String getUserSettingsFile(String path) {
        return "[]";
    }

    @JavascriptInterface
    public void changeSettingFile(String json) {

    }

    @JavascriptInterface
    public void changeServURL(String url) {

    }

    @JavascriptInterface
    public void loadBackgroundWeb(String url) {
        Handler mainHandler = new Handler(mContext.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                WebView wv = mainActivity.findViewById(R.id.bgwb);
                wv.loadUrl(url);
                wv.getSettings().setJavaScriptEnabled(true);
            } // This is your code
        };
        mainHandler.post(myRunnable);
    }

    HashMap<String, String> requestGET = new HashMap<>();

    @JavascriptInterface
    public String httpRequestGET(String url) {
        if(!requestGET.containsKey(url)) {
            try {
                OkHttpClient client = new OkHttpClient();
                /*try {
                    client.networkInterceptors().add(new Interceptor() {
                        @Override
                        public Response intercept(Interceptor.Chain chain) throws IOException {
                            return chain.proceed(chain.request());
                        }
                    });
                } catch (Exception e) {
                }*/
                Request.Builder build = new Request.Builder().url(url);
                Request req = build.build();
                Response resp = client.newCall(req).execute();
                String nhtml = Objects.requireNonNull(resp.body()).string();
                requestGET.put(url, nhtml);
                return nhtml;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        else {
            return requestGET.get(url);
        }
    }

    @JavascriptInterface
    public void syncCookies() {
        Log.e("fdqfsdqfsq", "ugçfsdsgfurieodugdfs");
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().acceptCookie();
        CookieManager.getInstance().flush();
    }

    @JavascriptInterface
    public void onUpdateStateChange(String cb) {
    }

    @JavascriptInterface
    public String getClientToken(String platform) {
        if(WebAppInterface.clientsToken.containsKey(platform)) return WebAppInterface.clientsToken.get(platform);
        else return null;
    }

    @JavascriptInterface
    public void openWebsiteInNewWindow(String baseUrl, String closeUrl) {
        Handler mainHandler = new Handler(mContext.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(mContext, WebViewPopup.class);
                i.putExtra("baseUrl", baseUrl);
                i.putExtra("closeUrl", closeUrl);
                mContext.startActivity(i);
            } // This is your code
        };
        mainHandler.post(myRunnable);
    }

    @JavascriptInterface
    public void searchUpdates() {
        Handler mainHandler = new Handler(mContext.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                view.evaluateJavascript("setTimeout(() => { updateCallBack({\n" +
                        "                    step: -1,\n" +
                        "                    file: null,\n" +
                        "                    cur: 1,\n" +
                        "                    max: 1\n" +
                        "                }) }, 1000)", null);
            } // This is your code
        };
        mainHandler.post(myRunnable);
    }

    @JavascriptInterface
    public void registerIframeUrl(String url, String script) {
        ScriptInjecter.addScript(url, script);
    }
}