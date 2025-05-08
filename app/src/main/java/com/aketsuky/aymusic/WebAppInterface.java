package com.aketsuky.aymusic;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
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
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WebAppInterface {
    Context mContext;
    WebView view;
    public static MainActivity mainActivity;
    static HashMap<String, String> clientsToken = new HashMap<>();
    public static MediaSession _mediaSession = null;
    public static String postData = null;
    final String MEDIA_SESSION_TAG = "AyMusic";
    String channelId = "AyMusicPlayer";
    MediaButtonIntentReceiver receiver;
    boolean itsMe = false;
    public static boolean registered = false;

    WebAppInterface(Context c, WebView wv, MainActivity main) {
        mContext = c;
        view = wv;
        mainActivity = main;
        _mediaSession = new MediaSession(c, MEDIA_SESSION_TAG);
        _mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        _mediaSession.setActive(true);
        ComponentName eventReceiver = new ComponentName(mContext.getPackageName(), MediaButtonIntentReceiver.class.getName());
        if (Build.VERSION.SDK_INT >= 31) {
            _mediaSession.setMediaButtonBroadcastReceiver(eventReceiver);
        }
        final boolean[] isPlaying = {false};
        final boolean[] debouncePause = {false};
        _mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() {
                Handler mainHandler = new Handler(mContext.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !isPlaying[0]) {
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
                                isPlaying[0] = true;
                                _mediaSession.getController().getTransportControls().play();
                            }
                        }*/
                        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !isPlaying[0]) {
                            AudioManager am = (AudioManager) MyService.instance.getSystemService(Context.AUDIO_SERVICE);
                            AudioFocusRequest afr = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                                    .setAudioAttributes(
                                            new AudioAttributes.Builder()
                                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                                    .build()
                                    )
                                    .setOnAudioFocusChangeListener(new AudioManager.OnAudioFocusChangeListener() {
                                        @Override
                                        public void onAudioFocusChange(int i) {
                                            Log.e("gfdsgdfgdsgdf", "" + i);
                                            if (i == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                                                // Pause playback
                                            } else if (i == AudioManager.AUDIOFOCUS_GAIN) {
                                                // Resume playback
                                                WebAppInterface._mediaSession.getController().getTransportControls().play();
                                            } else if (i == AudioManager.AUDIOFOCUS_LOSS) {
                                                // Stop playback
                                            }
                                        }
                                    })
                                    .build();
                            //int aa = am.requestAudioFocus(afr,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
                            int aa = am.requestAudioFocus(afr);
                            if (aa == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                                //WebAppInterface._mediaSession.getController().getTransportControls().play();
                            }
                        }*/
                        view.evaluateJavascript("listeners.player.play()", null);
                        isPlaying[0] = false;
                    } // This is your code
                };
                mainHandler.post(myRunnable);
                //mediaNotify();
            }

            @Override
            public void onPause() {
                Handler mainHandler = new Handler(mContext.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if(!debouncePause[0]) {
                            view.evaluateJavascript("listeners.player.pause()", null);
                        }
                        debouncePause[0] = false;
                    } // This is your code
                };
                mainHandler.post(myRunnable);
                //mediaNotify();
            }

            @Override
            public void onStop() {
                Handler mainHandler = new Handler(mContext.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        view.evaluateJavascript("listeners.player.pause()", null);
                    } // This is your code
                };
                mainHandler.post(myRunnable);
                //mediaNotify();
            }

            @Override
            public void onSkipToNext() {
                Handler mainHandler = new Handler(mContext.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        view.evaluateJavascript("listeners.player.next()", null);
                    } // This is your code
                };
                mainHandler.post(myRunnable);
                //mediaNotify();
            }

            @Override
            public void onSkipToPrevious() {
                Handler mainHandler = new Handler(mContext.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        view.evaluateJavascript("listeners.player.previous()", null);
                    } // This is your code
                };
                mainHandler.post(myRunnable);
                //mediaNotify();
            }

            @Override
            public boolean onMediaButtonEvent(final Intent mediaButtonIntent) {
                KeyEvent keyEvent = (KeyEvent) mediaButtonIntent.getExtras().get(Intent.EXTRA_KEY_EVENT);
                assert keyEvent != null;
                Log.d("aa", "GOT MediaButton EVENT " + keyEvent.getKeyCode() + " action: " + keyEvent.getAction());
                if(keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY ||
                            keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PAUSE ||
                            keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                        if (_mediaSession.getController().getPlaybackState().getState() == PlaybackState.STATE_PLAYING) {
                            view.evaluateJavascript("listeners.player.pause()", null);
                        } else {
                            debouncePause[0] = true;
                            view.evaluateJavascript("listeners.player.play()", null);
                        }
                    }
                }
                return super.onMediaButtonEvent(mediaButtonIntent);
            }

            @Override
            public void onSeekTo(long pos) {
                Handler mainHandler = new Handler(mContext.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        view.evaluateJavascript("listeners.player.seek(" + pos + ")", null);
                    } // This is your code
                };
                mainHandler.post(myRunnable);
                //mediaNotify();
            }
        });
        receiver = new MediaButtonIntentReceiver();
        receiver.setWb(wv);
        ContextCompat.registerReceiver(mContext, receiver, new IntentFilter("mediaPause"), ContextCompat.RECEIVER_NOT_EXPORTED);
        ContextCompat.registerReceiver(mContext, receiver, new IntentFilter("mediaPlay"), ContextCompat.RECEIVER_NOT_EXPORTED);
        ContextCompat.registerReceiver(mContext, receiver, new IntentFilter("mediaNext"), ContextCompat.RECEIVER_NOT_EXPORTED);
        ContextCompat.registerReceiver(mContext, receiver, new IntentFilter("mediaPrevious"), ContextCompat.RECEIVER_NOT_EXPORTED);
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

    @JavascriptInterface
    public void saveData(String path, byte[] data) {
        try {
            int i = path.lastIndexOf("/");
            if(i < 0) i = 0;
            File dir = new File(mainActivity.getDataDir() + "/" + path.substring(0, i));
            if (!dir .exists()) {
                dir.mkdirs();
            }
            File tempFile = new File(mainActivity.getDataDir() + "/" + path);
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(data);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void saveCache(String path, byte[] data) {
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

    @JavascriptInterface
    public void sessionChangeMediaMetadata(String title, String album, String artist, String artwork) {
        boolean changeSession = !title.equals(this.title) && !artist.equals(this.artist) && !album.equals(this.album);
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.artwork = artwork;
        if(changeSession) {
            _mediaSession.setMetadata(
                    new MediaMetadata.Builder()
                            .putString(MediaMetadata.METADATA_KEY_TITLE, title)
                            .putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
                            .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, getBitmapFromURL(artwork))
                            .putString(MediaMetadata.METADATA_KEY_ALBUM, album)
                            .putLong(MediaMetadata.METADATA_KEY_DURATION, duration)
                            .build()
            );
            mediaNotify();
        }
    }

    @JavascriptInterface
    public void sessionChangePositionState(int position, int duration, int playbackRate) {
        boolean changeDur = duration != this.duration;
        this.position = position;
        this.duration = duration;
        this.playbackRate = playbackRate;
        PlaybackState state = new PlaybackState.Builder()
                .setActions(
                        (playing ? PlaybackState.ACTION_PLAY : PlaybackState.ACTION_PAUSE) | PlaybackState.ACTION_PLAY_PAUSE |
                                PlaybackState.ACTION_SKIP_TO_NEXT | PlaybackState.ACTION_SKIP_TO_PREVIOUS | PlaybackState.ACTION_SEEK_TO)
                .setState(playing ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED, position, playbackRate, SystemClock.elapsedRealtime())
                .build();
        _mediaSession.setPlaybackState(state);
        if(changeDur) {
            _mediaSession.setMetadata(
                    new MediaMetadata.Builder()
                            .putString(MediaMetadata.METADATA_KEY_TITLE, title)
                            .putString(MediaMetadata.METADATA_KEY_ARTIST, artist)
                            .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, getBitmapFromURL(artwork))
                            .putString(MediaMetadata.METADATA_KEY_ALBUM, album)
                            .putLong(MediaMetadata.METADATA_KEY_DURATION, duration)
                            .build()
            );
            mediaNotify();
        }
    }

    @JavascriptInterface
    public void sessionChangePlaying(boolean playing) {
        this.playing = playing;
        PlaybackState state = new PlaybackState.Builder()
                .setActions(
                        (playing ? PlaybackState.ACTION_PLAY : PlaybackState.ACTION_PAUSE) | PlaybackState.ACTION_PLAY_PAUSE |
                                PlaybackState.ACTION_SKIP_TO_NEXT | PlaybackState.ACTION_SKIP_TO_PREVIOUS | PlaybackState.ACTION_SEEK_TO)
                .setState(playing ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED, position, playbackRate, SystemClock.elapsedRealtime())
                .build();
        _mediaSession.setPlaybackState(state);
        mediaNotify();
        if(MyService.instance == null && playing && WebAppInterface.aNoti != null) {
            Intent myService = new Intent(mainActivity, MyService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                view.getContext().startForegroundService(myService);
            }
        }
    }

    static Notification aNoti = null;

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
                    .setContentIntent(resultPendingIntent)
                    //.setNotificationSilent()
                    .setSound(null)
                    .setStyle(new Notification.MediaStyle()
                            .setShowActionsInCompactView(0, 1, 2)
                            .setMediaSession(_mediaSession.getSessionToken()))
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