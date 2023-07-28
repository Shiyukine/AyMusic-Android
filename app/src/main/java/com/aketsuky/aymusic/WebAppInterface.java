package com.aketsuky.aymusic;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.service.notification.NotificationListenerService;
import android.util.JsonReader;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.MimeTypeMap;
import android.webkit.ValueCallback;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import kotlin.Unit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WebAppInterface {
    Context mContext;
    WebView view;
    MainActivity mainActivity;
    static HashMap<String, String> clientsToken = new HashMap<>();
    private MediaSession _mediaSession = null;
    final String MEDIA_SESSION_TAG = "AyMusic";
    String channelId = "AyMusicPlayer";
    MediaButtonIntentReceiver receiver;
    boolean itsMe = false;

    WebAppInterface(Context c, WebView wv, MainActivity main) {
        mContext = c;
        view = wv;
        mainActivity = main;
        _mediaSession = new MediaSession(c, MEDIA_SESSION_TAG);
        _mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        _mediaSession.setActive(true);
        _mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() {
                Handler mainHandler = new Handler(mContext.getMainLooper());

                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        view.evaluateJavascript("listeners.player.play()", null);
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
                        view.evaluateJavascript("listeners.player.pause()", null);
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
                Log.d("aa", "GOT MediaButton EVENT");
                KeyEvent keyEvent = (KeyEvent) mediaButtonIntent.getExtras().get(Intent.EXTRA_KEY_EVENT);
                // ...do something with keyEvent, super... does nothing.
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
        mContext.registerReceiver(receiver, new IntentFilter("mediaPause"));
        mContext.registerReceiver(receiver, new IntentFilter("mediaPlay"));
        mContext.registerReceiver(receiver, new IntentFilter("mediaNext"));
        mContext.registerReceiver(receiver, new IntentFilter("mediaPrevious"));
    }

    @JavascriptInterface
    public String getSettingFile() {
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
        Log.d("fdqfsdqfsq", "ugçfsdsgfurieodugdfs");
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
    public void removeClientToken(String platform) {
        WebAppInterface.clientsToken.remove(platform);
    }

    @JavascriptInterface
    public void saveData(String path, byte[] data) {
        try {
            File dir = new File(mainActivity.getDataDir() + "/" + path.substring(0, path.lastIndexOf("/")));
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
            File dir = new File(mainActivity.getCacheDir() + "/" + path.substring(0, path.lastIndexOf("/")));
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
    @SuppressLint("StaticFieldLeak")
    public void searchUpdates() {
        Handler mainHandler = new Handler(mContext.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                view.evaluateJavascript("updateCallBack({\n" +
                        "                    step: 0,\n" +
                        "                    file: '" + Updates.servUrl + "/dl/AyMusic/update_android.json" + "',\n" +
                        "                    cur: 0,\n" +
                        "                    max: 100\n" +
                        "                })", null);
            } // This is your code
        };
        mainHandler.post(myRunnable);
        OkHttpClient client = new OkHttpClient();
        Request.Builder build = new Request.Builder().url(Updates.servUrl + "/dl/AyMusic/update_android.json");
        Request req = build.build();
        Response resp = null;
        try {
            resp = client.newCall(req).execute();
            String nhtml = Objects.requireNonNull(resp.body()).string();
            JSONObject json = new JSONObject(nhtml);
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        view.evaluateJavascript("updateCallBack({\n" +
                                "                    step: 0,\n" +
                                "                    file: '" + Updates.servUrl + "/dl/AyMusic/update_android.json" + "',\n" +
                                "                    cur: 50,\n" +
                                "                    max: 100\n" +
                                "                })", null);
                        String info = json.getString("***INFOS***");
                        int code = json.getInt("versionCode");
                        Log.e("fdedfsdqqsdfsdq", code + "");
                        view.evaluateJavascript("updateCallBack({\n" +
                                "                    step: 1,\n" +
                                "                    file: 'this APK',\n" +
                                "                    cur: 0,\n" +
                                "                    max: 100\n" +
                                "                })", null);
                        if (code > BuildConfig.VERSION_CODE) {
                            view.evaluateJavascript("updateCallBack({\n" +
                                    "                    step: 3,\n" +
                                    "                    file: 'this APK',\n" +
                                    "                    cur: 1,\n" +
                                    "                    max: 1\n" +
                                    "                })", null);
                            new downloadFile(mainActivity, "/updated.apk") {
                                @Override
                                protected void onProgressUpdate(String... values) {
                                    view.evaluateJavascript("updateCallBack({\n" +
                                            "                    step: 4,\n" +
                                            "                    file: '" + info.replace("%file%", "app.apk") + "',\n" +
                                            "                    cur: " + values[0] + ",\n" +
                                            "                    max: " + values[1] + "\n" +
                                            "                })", null);
                                }

                                @Override
                                protected void onPostExecute(File file) {
                                    view.evaluateJavascript("updateCallBack({\n" +
                                            "                    step: 5,\n" +
                                            "                    file: '" + info.replace("%file%", "app.apk") + "',\n" +
                                            "                    cur: 1,\n" +
                                            "                    max: 1\n" +
                                            "                })", null);
                                    Uri fileUri = FileProvider.getUriForFile(mainActivity, mainActivity.getApplicationContext().getPackageName() + ".provider", file);
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
                                    //mainActivity.startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:".concat("com.aketsuky.aymusic"))));
                                    mainActivity.startActivity(intent);
                                }
                            }.execute(info.replace("%file%", "app.apk"));
                        } else {
                            view.evaluateJavascript("updateCallBack({\n" +
                                    "                    step: -1,\n" +
                                    "                    file: null,\n" +
                                    "                    cur: 1,\n" +
                                    "                    max: 1\n" +
                                    "                })", null);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        view.evaluateJavascript("updateCallBack({\n" +
                                "                    step: -2,\n" +
                                "                    file: null,\n" +
                                "                    cur: 0,\n" +
                                "                    max: 1,\n" +
                                "                    error: `" + Arrays.toString(e.getStackTrace()) + "`" +
                                "                })", null);
                    }
                } // This is your code
            });
        } catch (Exception e) {
            e.printStackTrace();
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    view.evaluateJavascript("updateCallBack({\n" +
                            "                    step: -2,\n" +
                            "                    file: null,\n" +
                            "                    cur: 0,\n" +
                            "                    max: 1,\n" +
                            "                    error: `" + e.getMessage().replace("<", "") + "`" +
                            "                })", null);
                } // This is your code
            });
        }
    }

    @JavascriptInterface
    public void registerIframeUrl(String url, String script) {
        ScriptInjecter.addScript(url, script);
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
                return null;
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
        boolean changeSession = !title.equals(this.title) && !artwork.equals(this.artwork);
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
    }

    static Notification aNoti = null;

    void mediaNotify() {
        Context context = mContext;
        ComponentName eventReceiver = new ComponentName(context.getPackageName(), MediaButtonIntentReceiver.class.getName());
        if (Build.VERSION.SDK_INT >= 31) {
            _mediaSession.setMediaButtonBroadcastReceiver(eventReceiver);
        }
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
                            PendingIntent.FLAG_MUTABLE
                    )))
                    .addAction(new Notification.Action(playing ? R.drawable.ic_baseline_pause_24 : R.drawable.ic_baseline_play_arrow_24, playing ? "Pause" : "Play", PendingIntent.getBroadcast(
                            context,
                            1,
                            new Intent(playing ? "mediaPause" : "mediaPlay"),
                            PendingIntent.FLAG_MUTABLE
                    )))
                    .addAction(new Notification.Action(R.drawable.ic_baseline_skip_next_24, "Next", PendingIntent.getBroadcast(
                            context,
                            1,
                            new Intent("mediaNext"),
                            PendingIntent.FLAG_MUTABLE
                    )))
                    .setContentIntent(resultPendingIntent)
                    //.setNotificationSilent()
                    .setSound(null)
                    .setStyle(new Notification.MediaStyle()
                            .setMediaSession(_mediaSession.getSessionToken()))
                    .setChannelId(channelId)
                    //.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
                    .build();
        }
        aNoti = noti;
        Intent myService = new Intent(mainActivity, MyService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mainActivity.startForegroundService(myService);
        }
        //nm.notify(0, noti);
    }
}