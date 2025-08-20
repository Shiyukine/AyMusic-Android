package com.aketsuky.aymusic;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.OptIn;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.session.CommandButton;
import androidx.media3.session.legacy.MediaMetadataCompat;
import androidx.media3.session.legacy.MediaSessionCompat;
import androidx.media3.session.legacy.PlaybackStateCompat;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;
import androidx.webkit.WebViewMediaIntegrityApiStatusConfig;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UnstableApi
public class WebAppInterface {
    Context mContext;
    WebView view;
    public static MainActivity mainActivity;
    static HashMap<String, String> clientsToken = new HashMap<>();
    @SuppressLint("RestrictedApi")
    public static MediaSessionCompat _mediaSession = null;
    public static String postData = null;
    final String MEDIA_SESSION_TAG = "AyMusic";
    String channelId = "AyMusicPlayer";
    //MediaButtonIntentReceiver receiver;
    public static boolean registered = false;

    @SuppressLint("RestrictedApi")
    WebAppInterface(Context c, WebView wv, MainActivity main) {
        mContext = c;
        view = wv;
        mainActivity = main;
        _mediaSession = new MediaSessionCompat(c, MEDIA_SESSION_TAG, null, null, null);
        _mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        _mediaSession.setActive(true);
        //test if working
        PlaybackStateCompat state = new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_NONE, 0, 0, SystemClock.elapsedRealtime())
                .build();
        _mediaSession.setPlaybackState(state);
        //
        PendingIntent pi = PendingIntent.getBroadcast(mainActivity, 0, new Intent(mainActivity, MainActivity.MediaButtonIntentReceiver.class), PendingIntent.FLAG_IMMUTABLE);
        _mediaSession.setMediaButtonReceiver(pi);
        Handler mainHandler = new Handler(mContext.getMainLooper());
        _mediaSession.setCallback(new MediaSessionCallback(view, _mediaSession), mainHandler);
        MediaButtonIntentReceiverNotification receiver = new MediaButtonIntentReceiverNotification();
        //receiver.setWb(wv);
        ContextCompat.registerReceiver(mContext, receiver, new IntentFilter("mediaPause"), ContextCompat.RECEIVER_NOT_EXPORTED);
        ContextCompat.registerReceiver(mContext, receiver, new IntentFilter("mediaPlay"), ContextCompat.RECEIVER_NOT_EXPORTED);
        ContextCompat.registerReceiver(mContext, receiver, new IntentFilter("mediaNext"), ContextCompat.RECEIVER_NOT_EXPORTED);
        ContextCompat.registerReceiver(mContext, receiver, new IntentFilter("mediaPrevious"), ContextCompat.RECEIVER_NOT_EXPORTED);
        ContextCompat.registerReceiver(mContext, receiver, new IntentFilter("shuffle_true"), ContextCompat.RECEIVER_NOT_EXPORTED);
        ContextCompat.registerReceiver(mContext, receiver, new IntentFilter("shuffle_false"), ContextCompat.RECEIVER_NOT_EXPORTED);
        ContextCompat.registerReceiver(mContext, receiver, new IntentFilter("repeat_0"), ContextCompat.RECEIVER_NOT_EXPORTED);
        ContextCompat.registerReceiver(mContext, receiver, new IntentFilter("repeat_1"), ContextCompat.RECEIVER_NOT_EXPORTED);
        ContextCompat.registerReceiver(mContext, receiver, new IntentFilter("repeat_2"), ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @JavascriptInterface
    public String getSettingFile() {
        WebAppInterface.registered = true;
        SharedPreferences set = mainActivity.getSharedPreferences("UserConfig", 0);
        return set.getString("json", "{}");
    }

    @JavascriptInterface
    public String getUserSettingsFile(String file) {
        SharedPreferences set = mainActivity.getSharedPreferences(file.replace("/", "_"), 0);
        return set.getString("json", "[]");
    }

    @JavascriptInterface
    public void changeSettingFile(String json) {
        SharedPreferences set = mainActivity.getSharedPreferences("UserConfig", 0);
        set.edit().putString("json", json).apply();
    }

    @JavascriptInterface
    public void changeUserSettingsFile(String file, String json) {
        SharedPreferences set = mainActivity.getSharedPreferences(file.replace("/", "_"), 0);
        set.edit().putString("json", json).apply();
    }

    @JavascriptInterface
    public void pickUpMusic() {
        Handler mainHandler = new Handler(mContext.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("audio/*");
                mainActivity.mGetContent.launch(intent);
            } // This is your code
        };
        mainHandler.post(myRunnable);
    }

    @JavascriptInterface
    public void changeServURL(String url) {
        Updates.servUrl = url;
    }

    @JavascriptInterface
    public void openLink(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        mainActivity.startActivity(browserIntent);
    }

    @JavascriptInterface
    public void loadBackgroundWeb(String url) {
        Handler mainHandler = new Handler(mContext.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @SuppressLint("SetJavaScriptEnabled")
            @Override
            public void run() {
                WebView webView2 = new WebView(mainActivity);
                FrameLayout fl = mainActivity.findViewById(R.id.fl);
                fl.addView(webView2);
                webView2.setWebChromeClient(new WebChromeClient() {
                    @Override
                    public void onPermissionRequest(PermissionRequest request) {
                        String[] resources = request.getResources();
                        for (int i = 0; i < resources.length; i++) {
                            if (PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID.equals(resources[i])) {
                                request.grant(resources);
                                return;
                            }
                        }

                        super.onPermissionRequest(request);
                    }
                });
                webView2.getSettings().setAllowFileAccess(true);
                webView2.getSettings().setAllowContentAccess(true);
                webView2.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onLoadResource(WebView view, String url) {
                        // TODO Auto-generated method stub
                        //Log.e("ddsqqsd", url);
                        view.evaluateJavascript("var _scr = {};\n" +
                                "        for (const key in screen) {\n" +
                                "            switch (key) {\n" +
                                "                case \"width\":\n" +
                                "                    _scr[key] = 1920;\n" +
                                "                    break;\n" +
                                "                case \"height\":\n" +
                                "                    _scr[key] = 1080;\n" +
                                "                    break;\n" +
                                "                case \"availWidth\":\n" +
                                "                    _scr[key] = 1920;\n" +
                                "                    break;\n" +
                                "                case \"availHeight\":\n" +
                                "                    _scr[key] = 1080;\n" +
                                "                    break;\n" +
                                "                default:\n" +
                                "                    _scr[key] = screen[key];\n" +
                                "                    break;\n" +
                                "            }\n" +
                                "        }\n" +
                                "        window.screen = _scr;", null);
                        super.onLoadResource(view, url);
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        CookieManager.getInstance().setAcceptCookie(true);
                        CookieManager.getInstance().acceptCookie();
                        CookieManager.getInstance().flush();
                    }

                    boolean debounceSpotify = false;

                    @OptIn(markerClass = UnstableApi.class)
                    @Override
                    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                        if (request.getUrl().toString().contains("https://api-auth.soundcloud.com/oauth/authorize")) {
                            Log.e("fdqfsdfsdq", " " + request.getUrl().getQueryParameter("client_id"));
                            WebAppInterface.clientsToken.put("Soundcloud", request.getUrl().getQueryParameter("client_id"));
                            Handler mainHandler = new Handler(mainActivity.getMainLooper());

                            Runnable myRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    webView2.destroy();
                                    fl.removeView(webView2);
                                } // This is your code
                            };
                            mainHandler.post(myRunnable);
                        }
                        if(request.getUrl().toString().contains("spotify.com")) {
                            for (Map.Entry<String, String> head : request.getRequestHeaders().entrySet()) {
                                if (head.getKey().toLowerCase().equals("authorization".toLowerCase())) {
                                    Log.e("fdqfsdfsdq", head.getValue() + " " + request.getUrl().toString());
                                    WebAppInterface.clientsToken.put("Spotify", head.getValue().split("Bearer ")[1]);
                                    if(request.getUrl().toString().contains("api-partner.spotify.com/pathfinder") && debounceSpotify) {
                                        Handler mainHandler = new Handler(mainActivity.getMainLooper());

                                        Runnable myRunnable = new Runnable() {
                                            @Override
                                            public void run() {
                                                webView2.destroy();
                                                fl.removeView(webView2);
                                                debounceSpotify = false;
                                            } // This is your code
                                        };
                                        mainHandler.post(myRunnable);
                                    }
                                    debounceSpotify = true;
                                }
                            }
                        }
                        return super.shouldInterceptRequest(view, request);
                    }
                });
                WebSettings webViewSettings2 = webView2.getSettings();
                webViewSettings2.setJavaScriptCanOpenWindowsAutomatically(false);
                webViewSettings2.setAllowUniversalAccessFromFileURLs(true);
                webViewSettings2.setAllowContentAccess(true);
                webViewSettings2.setAllowFileAccessFromFileURLs(true);
                webViewSettings2.setDomStorageEnabled(true);
                webViewSettings2.setDatabaseEnabled(true);
                webViewSettings2.setMediaPlaybackRequiresUserGesture(false);
                webViewSettings2.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36");
                webViewSettings2.setJavaScriptEnabled(true);
                CookieManager.getInstance().setAcceptCookie(true);
                CookieManager.getInstance().setAcceptThirdPartyCookies(webView2,true);
                if (WebViewFeature.isFeatureSupported(WebViewFeature.WEBVIEW_MEDIA_INTEGRITY_API_STATUS)) {
                    // Create a configuration that disables the Media Integrity API by default
                    WebViewMediaIntegrityApiStatusConfig config =
                            new WebViewMediaIntegrityApiStatusConfig.Builder(
                                    WebViewMediaIntegrityApiStatusConfig.WEBVIEW_MEDIA_INTEGRITY_API_DISABLED
                            ).build();
                    WebSettingsCompat.setWebViewMediaIntegrityApiStatus(webViewSettings2, config);
                }
                webView2.loadUrl(url);
            } // This is your code
        };
        mainHandler.post(myRunnable);
    }

    HashMap<String, String> requestGET = new HashMap<>();

    @JavascriptInterface
    @SuppressLint("StaticFieldLeak")
    public void httpRequestGET(String url) {
        if(!requestGET.containsKey(url)) {
            try {
                new getData() {
                    @Override
                    protected void onPostExecute(String s) {
                        super.onPostExecute(s);
                        if(s != null && !s.equals("")) {
                            view.evaluateJavascript("window.listeners.httpRequestCallback(`" + s.replace("\\", "\\\\").replace("${", "\\${").replace("`", "\\`") + "`)", null);
                        }
                        else {
                            view.evaluateJavascript("window.listeners.httpRequestCallback('')", null);
                        }
                    }
                }.execute(url);
                /*OkHttpClient client = new OkHttpClient();
                /*try {
                    client.networkInterceptors().add(new Interceptor() {
                        @Override
                        public Response intercept(Interceptor.Chain chain) throws IOException {
                            return chain.proceed(chain.request());
                        }
                    });
                } catch (Exception e) {
                }*
                Request.Builder build = new Request.Builder().url(url);
                Request req = build.build();
                Response resp = client.newCall(req).execute();
                String nhtml = Objects.requireNonNull(resp.body()).string();
                requestGET.put(url, nhtml);
                return nhtml;*/
            } catch (Exception e) {
                e.printStackTrace();
                view.evaluateJavascript("window.listeners.httpRequestCallback('')", null);
            }
        }
        else {
            view.evaluateJavascript("window.listeners.httpRequestCallback(`" + requestGET.get(url).replace("\\", "\\\\").replace("${", "\\${").replace("`", "\\`") + "`)", null);
        }
    }

    @JavascriptInterface
    @SuppressLint({"StaticFieldLeak"})
    public String httpRequestPOST(String url, String data, String contentType) {
        try {
            String urlDom = new URL(url).getHost();
            if(urlDom.split("\\.").length > 2) {
                String tmp = "";
                String[] spl = urlDom.split("\\.");
                for(int i = spl.length - 2; i < spl.length; i++) {
                    tmp += spl[i] + ".";
                }
                urlDom = tmp.substring(0, tmp.length() - 1);
            }
            //urlDom = "https://" + new URL(url).getHost() + "/";
            Log.e("fsfsdfsdfdddd", urlDom);
            HttpURLConnection connection = (HttpURLConnection)(new URL(url)).openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setInstanceFollowRedirects(false);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.addRequestProperty("Content-Type", contentType);
            OutputStream os = connection.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            osw.write(data);
            osw.flush();
            osw.close();
            os.close();
            connection.connect();
            for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
                String h = entry.getKey();
                if(h != null && h.toLowerCase().equals("set-cookie")) {
                    for(String val : entry.getValue()) {
                        String nval = val.replace("; Secure", "").replace("; SameSite=lax", "") + "; SameSite=None; Partitioned";
                        Log.e("fsfsdfsfs", nval);
                        String[] info = nval.split(";");
                        String domain = info[1].split("=")[1];
                        Log.e("fsfsdfsfs", domain);
                        CookieManager.getInstance().setCookie(urlDom, val);
                    }
                }
            }

            CookieManager.getInstance().flush();

            BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
            ByteArrayOutputStream buf = new ByteArrayOutputStream();

            for(int result2 = bis.read(); result2 != -1; result2 = bis.read()) {
                buf.write((byte)result2);
            }

            String result = buf.toString();
            return result;
        } catch (Exception var15) {
            var15.printStackTrace();
            return "mmh";
        }
    }

    @JavascriptInterface
    public void setPostData(String data, String contentType) {
        WebAppInterface.postData = data;
    }

    @JavascriptInterface
    public void syncCookies() {
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().acceptCookie();
        CookieManager.getInstance().flush();
    }

    @JavascriptInterface
    public String haveCookie(String siteName, String cookieName){
        String CookieValue = null;
        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie(siteName);
        if(cookies != null) {
            String[] temp = cookies.split(";");
            HashMap<String, String> out = new HashMap<>();
            for (String ar1 : temp) {
                if (ar1.contains(cookieName)) {
                    return ar1.split("=")[1];
                }
            }
        }
        return "";
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
    public void removeClientToken(String platform) {
        WebAppInterface.clientsToken.remove(platform);
    }

    @SuppressLint("StaticFieldLeak")
    @JavascriptInterface
    public void saveData(String path, byte[] data) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    int i = path.lastIndexOf("/");
                    if (i < 0) i = 0;
                    File dir = new File(mainActivity.getDataDir() + "/" + path.substring(0, i));
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File tempFile = new File(mainActivity.getDataDir() + "/" + path);
                    FileOutputStream fos = new FileOutputStream(tempFile);
                    fos.write(data);
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    @JavascriptInterface
    public void saveCache(String path, byte[] data) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    int i = path.lastIndexOf("/");
                    if(i < 0) i = 0;
                    File dir = new File(mainActivity.getCacheDir() + "/" + path.substring(0, i));
                    if (!dir .exists()) {
                        dir.mkdirs();
                    }
                    File tempFile = new File(mainActivity.getCacheDir() + "/" + path);
                    FileOutputStream fos = new FileOutputStream(tempFile);
                    fos.write(data);
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    @JavascriptInterface
    public boolean removeCache(String path) {
        try {
            int i = path.lastIndexOf("/");
            if(i < 0) i = 0;
            File dir = new File(mainActivity.getCacheDir() + "/" + path.substring(0, i));
            if (dir .exists()) {
                return Utils.deleteDir(dir);
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @JavascriptInterface
    public boolean removeData(String path) {
        try {
            int i = path.lastIndexOf("/");
            if(i < 0) i = 0;
            File dir = new File(mainActivity.getDataDir() + "/" + path.substring(0, i));
            if (dir .exists()) {
                return Utils.deleteDir(dir);
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static HashMap<String, Boolean> bpWR = new HashMap<>();

    @JavascriptInterface
    public void addBypassWebRequest(String url) {
        if(!bpWR.containsKey(url)) bpWR.put(url, false);
    }

    @JavascriptInterface
    public void addBypassWebRequest(String url, boolean includes) {
        if(!includes) addBypassWebRequest(url);
        else if(!bpWR.containsKey(url)) bpWR.put(url, true);
    }

    public static HashMap<String, Boolean> interceptAll = new HashMap<>();

    @JavascriptInterface
    public void addInterceptAllWebRequest(String url) {
        if(!interceptAll.containsKey(url)) interceptAll.put(url, false);
    }

    @JavascriptInterface
    public void addInterceptAllWebRequest(String url, boolean includes) {
        if(!includes) addInterceptAllWebRequest(url);
        else if(!interceptAll.containsKey(url)) interceptAll.put(url, true);
    }

    @JavascriptInterface
    public void openWebsiteInNewWindow(String baseUrl, String closeUrl, boolean filterByInclude) {
        Handler mainHandler = new Handler(mContext.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(mContext, WebViewPopup.class);
                i.putExtra("baseUrl", baseUrl);
                i.putExtra("closeUrl", closeUrl);
                i.putExtra("filterByInclude", filterByInclude);
                mContext.startActivity(i);
            } // This is your code
        };
        mainHandler.post(myRunnable);
    }

    @JavascriptInterface
    @SuppressLint("StaticFieldLeak")
    public void searchUpdates() {
        Updates.searchUpdates(mContext, view, mainActivity);
    }

    @JavascriptInterface
    public void addBadUrl(String[] urls) {
        throw new RuntimeException("deprecated. use addBadUrl(string url, boolean includes)");
    }

    @JavascriptInterface
    public void addBadUrl(String url, boolean includes) {
        Adblock.urlBlocked.put(url, includes);
    }

    @JavascriptInterface
    public void registerIframeUrl(String url, String script) {
        ScriptInjecter.addScript(url, script);
    }

    @JavascriptInterface
    public void registerOverrideResponse(String json) throws JSONException {
        ScriptInjecter.addOverrideResponse(json);
    }

    @JavascriptInterface
    public int getIframeStatus(String url) {
        //status: 0 - not loaded, 1 - loaded, 2 - failed
        return ScriptInjecter.getUrlStatus(url);
    }

    HashMap<String, Bitmap> bitmaps = new HashMap<>();

    private Bitmap getBitmapFromURL(String strURL) {
        if(bitmaps.containsKey(strURL)) return bitmaps.get(strURL);
        else {
            try {
                if (strURL.startsWith("/")) {
                    String newUrl = strURL.substring(1);
                    AssetManager am = mainActivity.getAssets();
                    InputStream is = am.open(newUrl);
                    Bitmap myBitmap = BitmapFactory.decodeStream(is);
                    bitmaps.put(strURL, myBitmap);
                    return myBitmap;
                }
                else if (strURL.startsWith("https://mycache/")) {
                    String newUrl = strURL.replace("https://mycache/", "");
                    File tempFile = new File(mainActivity.getCacheDir() + "/" + newUrl);
                    FileInputStream is = new FileInputStream(tempFile);
                    Bitmap myBitmap = BitmapFactory.decodeStream(is);
                    bitmaps.put(strURL, myBitmap);
                    return myBitmap;
                }
                else if (strURL.startsWith("https://mydata/")) {
                    String newUrl = strURL.replace("https://mydata/", "");
                    File tempFile = new File(mainActivity.getDataDir() + "/" + newUrl);
                    FileInputStream is = new FileInputStream(tempFile);
                    Bitmap myBitmap = BitmapFactory.decodeStream(is);
                    bitmaps.put(strURL, myBitmap);
                    return myBitmap;
                }
                else {
                    URL url = new URL(strURL);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap myBitmap = BitmapFactory.decodeStream(input);
                    bitmaps.put(strURL, myBitmap);
                    return myBitmap;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                try {
                    String newUrl = "/resources/icon.ico".substring(1);
                    AssetManager am = mainActivity.getAssets();
                    InputStream is = am.open(newUrl);
                    Bitmap myBitmap = BitmapFactory.decodeStream(is);
                    bitmaps.put(strURL, myBitmap);
                    return myBitmap;
                }
                catch (Exception ex2) {
                    ex2.printStackTrace();
                    return null;
                }
            }
        }
    }

    String title = "";
    String album = "";
    String artist = "";
    String artwork = "";
    int position = 0;
    int duration = 0;
    int playbackRate = 1;
    boolean playing = false;
    boolean shuffling = false;
    int repeat = 0;

    @SuppressLint("RestrictedApi")
    @JavascriptInterface
    public void sessionChangeMediaMetadata(String title, String album, String artist, String artwork) {
        boolean changeTitle = !title.equals(this.title);
        boolean changeAlbum = !album.equals(this.album);
        boolean changeArtist = !artist.equals(this.artist);
        boolean changeArtwork = !artwork.equals(this.artwork);
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.artwork = artwork;
        if(changeTitle || changeAlbum || changeArtist || changeArtwork) {
            refreshMediaSession();
        }
    }

    @SuppressLint("RestrictedApi")
    @JavascriptInterface
    public void sessionChangePositionState(int position, int duration, int playbackRate, boolean isPlaying, boolean shuffling, int repeat) {
        boolean changeDur = duration != this.duration;
        boolean changeRepeat = repeat != this.repeat;
        boolean changeShuffle = shuffling != this.shuffling;
        this.position = position;
        this.duration = duration;
        this.playbackRate = playbackRate;
        this.playing = isPlaying;
        this.shuffling = shuffling;
        this.repeat = repeat;
        int shufflingIcon = shuffling ? R.drawable.baseline_shuffle_on_24 : R.drawable.baseline_shuffle_24;
        Bundle shuffleExtras = new Bundle();
        shuffleExtras.putInt(
                androidx.media3.session.MediaConstants.EXTRAS_KEY_COMMAND_BUTTON_ICON_COMPAT,
                shuffling ? CommandButton.ICON_SHUFFLE_ON : CommandButton.ICON_SHUFFLE_OFF);
        int repeatIcon = repeat == 0 ? R.drawable.baseline_repeat_24 : repeat == 1 ? R.drawable.baseline_repeat_on_24 : R.drawable.baseline_repeat_one_on_24;
        Bundle repeatExtras = new Bundle();
        repeatExtras.putInt(
                androidx.media3.session.MediaConstants.EXTRAS_KEY_COMMAND_BUTTON_ICON_COMPAT,
                repeat == 0 ? CommandButton.ICON_REPEAT_OFF : repeat == 1 ? CommandButton.ICON_REPEAT_ONE : CommandButton.ICON_REPEAT_ALL);
        PlaybackStateCompat state = new PlaybackStateCompat.Builder()
                .setActions((!isPlaying ? PlaybackStateCompat.ACTION_PLAY : PlaybackStateCompat.ACTION_PAUSE) |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS | PlaybackStateCompat.ACTION_SEEK_TO | PlaybackStateCompat.ACTION_STOP | PlaybackStateCompat.ACTION_SET_REPEAT_MODE | PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE)
                .addCustomAction(new PlaybackStateCompat.CustomAction.Builder("shuffle_" + shuffling, "Shuffle", shufflingIcon).setExtras(shuffleExtras).build())
                .addCustomAction(new PlaybackStateCompat.CustomAction.Builder("repeat_" + repeat, "Repeat", repeatIcon).setExtras(repeatExtras).build())
                .setState(isPlaying ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED, position, playbackRate, SystemClock.elapsedRealtime())
                .build();
        _mediaSession.setPlaybackState(state);
        if(changeDur || changeRepeat || changeShuffle) {
            refreshMediaSession();
        }
        if(MyService.instance == null && WebAppInterface.aNoti != null) {
            Intent myService = new Intent(mainActivity, MyService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                OneTimeWorkRequest request = new OneTimeWorkRequest.Builder ( BackupWorker.class ).addTag ( "BACKUP_WORKER_TAG" ).build ();
                WorkManager.getInstance ( view.getContext() ).enqueue ( request );
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                view.getContext().startForegroundService(myService);
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private void refreshMediaSession()
    {
        final Bitmap albumArtBitmap = getBitmapFromURL(artwork);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                _mediaSession.setMetadata(
                        new MediaMetadataCompat.Builder()
                                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title)
                                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, artist)
                                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, album)
                                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArtBitmap)
                                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                                .build()
                );
                _mediaSession.setRepeatMode(repeat == 0 ? PlaybackStateCompat.REPEAT_MODE_NONE : repeat == 1 ? PlaybackStateCompat.REPEAT_MODE_ONE : PlaybackStateCompat.REPEAT_MODE_ALL);
                _mediaSession.setShuffleMode(shuffling ? PlaybackStateCompat.SHUFFLE_MODE_ALL : PlaybackStateCompat.SHUFFLE_MODE_NONE);
                mediaNotify();
            }
        });
    }

    @JavascriptInterface
    public void sessionChangePlaying(boolean playing) {
        this.playing = playing;
        /*PlaybackState state = new PlaybackState.Builder()
                .setState(playing ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED, position, playbackRate, SystemClock.elapsedRealtime())
                .build();
        _mediaSession.setPlaybackState(state);*/
        if(MyService.instance != null) {
            if (!playing) {
                ((AudioManager) MyService.instance.getSystemService(
                        Context.AUDIO_SERVICE)).requestAudioFocus(
                        null,
                        AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            }
            mediaNotify();
        }
    }

    static Notification aNoti = null;

    @SuppressLint("RestrictedApi")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @JavascriptInterface
    public void audioPriority() {
        AudioManager am = (AudioManager)MyService.instance.getSystemService(Context.AUDIO_SERVICE);
        AudioFocusRequest afr = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                        new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                )
                .build();
        //int aa = am.requestAudioFocus(afr,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
        int aa = am.requestAudioFocus(afr);
        if(aa == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            _mediaSession.getController().getTransportControls().play();
        }
        Log.e("fsdfsqf", aa + "");
    }

    public static String windowInsetsJSON = "{\"left\": 0, \"top\": 0, \"right\": 0, \"bottom\": 0}";

    @JavascriptInterface
    public String getWindowInsets() {
        return windowInsetsJSON;
    }

    @SuppressLint("RestrictedApi")
    void mediaNotify() {
        Context context = mContext;
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(
                    channelId,
                    "AyMusic playing notification",
                    NotificationManager.IMPORTANCE_LOW);
            nm.createNotificationChannel(channel);
        }
        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.putExtra("showListen", true);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent resultPendingIntent = null;
        if (Build.VERSION.SDK_INT >= 31) {
            resultPendingIntent = PendingIntent.getActivity(
                            mContext, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }
        else {
            resultPendingIntent = PendingIntent.getActivity(
                    mContext, 0, resultIntent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_UPDATE_CURRENT);
        }
        Notification noti = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && _mediaSession.getController().getMetadata() != null) {
            noti = new Notification.Builder(context, channelId)
                    .setSmallIcon(R.drawable.ic_stat_name)
                    .setContentTitle(_mediaSession.getController().getMetadata().getString(MediaMetadata.METADATA_KEY_TITLE))
                    .setContentText(_mediaSession.getController().getMetadata().getString(MediaMetadata.METADATA_KEY_ARTIST))
                    //.setLargeIcon(R.drawable.ic_launcher_foreground)
                    .setAutoCancel(playing)
                    .setOngoing(playing)
                    .addAction(new Notification.Action(shuffling ? R.drawable.baseline_shuffle_on_24 : R.drawable.baseline_shuffle_24, "Shuffle", PendingIntent.getBroadcast(
                            context,
                            1,
                            new Intent("shuffle_" + shuffling),
                            PendingIntent.FLAG_IMMUTABLE
                    )))
                    .addAction(new Notification.Action(R.drawable.ic_baseline_skip_previous_24, "Previous", PendingIntent.getBroadcast(
                            context,
                            1,
                            new Intent("mediaPrevious"),
                            PendingIntent.FLAG_IMMUTABLE
                    )))
                    .addAction(new Notification.Action(playing ? R.drawable.ic_baseline_pause_24 : R.drawable.ic_baseline_play_arrow_24, playing ? "Pause" : "Play", PendingIntent.getBroadcast(
                            context,
                            1,
                            new Intent(playing ? "mediaPause" : "mediaPlay"),
                            PendingIntent.FLAG_IMMUTABLE
                    )))
                    .addAction(new Notification.Action(R.drawable.ic_baseline_skip_next_24, "Next", PendingIntent.getBroadcast(
                            context,
                            1,
                            new Intent("mediaNext"),
                            PendingIntent.FLAG_IMMUTABLE
                    )))
                    .addAction(new Notification.Action(repeat == 0 ? R.drawable.baseline_repeat_24 : repeat == 1 ? R.drawable.baseline_repeat_on_24 : R.drawable.baseline_repeat_one_on_24, "Repeat", PendingIntent.getBroadcast(
                            context,
                            1,
                            new Intent("repeat_" + repeat),
                            PendingIntent.FLAG_IMMUTABLE
                    )))
                    .setContentIntent(resultPendingIntent)
                    //.setNotificationSilent()
                    .setSound(null)
                    .setStyle(new Notification.MediaStyle()
                            .setShowActionsInCompactView(0, 1, 2)
                            .setMediaSession(_mediaSession.getSessionToken().getToken()))
                    .setChannelId(channelId)
                    //.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
                    .build();
        }
        aNoti = noti;
        if(MyService.instance == null) {

        }
        else {
            MyService.instance.updateNotif();
        }
        //nm.notify(0, noti);
    }
}