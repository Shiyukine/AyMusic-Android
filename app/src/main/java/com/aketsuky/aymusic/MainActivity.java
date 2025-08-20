package com.aketsuky.aymusic;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.util.UnstableApi;
import androidx.webkit.ServiceWorkerClientCompat;
import androidx.webkit.ServiceWorkerControllerCompat;
import androidx.webkit.WebNavigationClient;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;
import androidx.webkit.WebViewMediaIntegrityApiStatusConfig;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.KeyguardManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Insets;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.MimeTypeMap;
import android.webkit.PermissionRequest;
import android.webkit.SafeBrowsingResponse;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Semaphore;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    public static class MediaButtonIntentReceiver extends BroadcastReceiver {

        public MediaButtonIntentReceiver() {
            super ();
        }

        public static final int OP_BACKGROUND_START_ACTIVITY = 10021;
        public static final int OP_SHOW_WHEN_LOCKED = 10020;

        @SuppressWarnings("JavaReflectionMemberAccess")
        public static boolean isBackgroundStartActivityPermissionGranted(Context context) {
            try {
                AppOpsManager mgr = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                Method m = AppOpsManager.class.getMethod("checkOpNoThrow", int.class, int.class, String.class);
                int result = (int) m.invoke(mgr, OP_BACKGROUND_START_ACTIVITY, android.os.Process.myUid(), context.getPackageName());
                return result == AppOpsManager.MODE_ALLOWED;
            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return true;
        }

        @SuppressWarnings("JavaReflectionMemberAccess")
        public static boolean isShowWhenLockedPermissionGranted(Context context) {
            try {
                AppOpsManager mgr = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                Method m = AppOpsManager.class.getMethod("checkOpNoThrow", int.class, int.class, String.class);
                int result = (int) m.invoke(mgr, OP_SHOW_WHEN_LOCKED, android.os.Process.myUid(), context.getPackageName());
                return result == AppOpsManager.MODE_ALLOWED;
            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return true;
        }

        static int first = 0;

        @SuppressLint("RestrictedApi")
        @OptIn(markerClass = UnstableApi.class)
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();
            Log.i("fdssgdsfg", "intentAction: " + intentAction);
            if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
                if(first == 0) {
                    KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                    if (!isBackgroundStartActivityPermissionGranted(context) || (myKM.inKeyguardRestrictedInputMode() && !isShowWhenLockedPermissionGranted(context))) {
                        int importance = NotificationManager.IMPORTANCE_HIGH;
                        String CHANNEL_ID = "AyMusicBgHelper";
                        NotificationChannel channel = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            channel = new NotificationChannel(CHANNEL_ID, "AyMusic background helper", importance);
                            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                            notificationManager.createNotificationChannel(channel);
                            Intent intentt = null;
                            if("xiaomi".equals(Build.MANUFACTURER.toLowerCase(Locale.ROOT))) {
                                intentt = new Intent("miui.intent.action.APP_PERM_EDITOR");
                                intentt.setClassName("com.miui.securitycenter",
                                        "com.miui.permcenter.permissions.PermissionsEditorActivity");
                                intentt.putExtra("extra_pkgname", context.getPackageName());
                            }
                            else {
                                intentt = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                                intentt.setData(uri);
                            }
                            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intentt, PendingIntent.FLAG_IMMUTABLE);
                            String text = myKM.inKeyguardRestrictedInputMode() && !isShowWhenLockedPermissionGranted(context) ? "You must allow \"Show on Lock screen\" and \"Display pop-up in background\" to use AyMusic in background. Click here to allow." : "You must allow \"Display pop-up in background\" to use AyMusic in background. Click here to allow.";
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                                    .setSmallIcon(R.drawable.ic_stat_name)
                                    .setContentTitle("AyMusic")
                                    .setContentText(text)
                                    .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                    .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                                    .setContentIntent(pendingIntent)
                                    .setAutoCancel(true);
                            NotificationManagerCompat.from(context).notify(2, builder.build());
                        }
                    }
                    else {
                        Log.i("fdssgdsfg", "no media button information, launching app instead.");
                        Intent intentt = new Intent(context.getApplicationContext(), MainActivity.class);
                        intentt.putExtra("enableAutoPlay", true);
                        intentt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.getApplicationContext().startActivity(intentt);
                    }
                }
                first++;
                if(first == 2) first = 0;
            }
            else {
                WebAppInterface._mediaSession.getController().getTransportControls().play();
            }
        }
    }

    Map<String, String> loadedAssets = new HashMap<>();
    static MediaWebView actualWb;
    ActivityResultLauncher<Intent> mGetContent;
    AudioManager.OnAudioFocusChangeListener amOn;

    @Override
    public Intent registerReceiver(@Nullable BroadcastReceiver receiver, IntentFilter filter) {
        if (Build.VERSION.SDK_INT >= 34 && getApplicationInfo().targetSdkVersion >= 34) {
            return super.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            return super.registerReceiver(receiver, filter);
        }
    }

    @SuppressLint("WrongConstant")
    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightNavigationBars(false);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView()).setAppearanceLightStatusBars(false);
        ViewCompat.setOnApplyWindowInsetsListener(this.getWindow().getDecorView(), new OnApplyWindowInsetsListener() {
            @NonNull
            @Override
            public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                androidx.core.graphics.Insets in = insets.getInsets(WindowInsetsCompat.Type.navigationBars()
                        | WindowInsetsCompat.Type.displayCutout());
                WebAppInterface.windowInsetsJSON = "{\"left\": " + in.left + ", \"top\": " + in.top + ", \"right\": " + in.right + ", \"bottom\": " + in.bottom + "}";
                return WindowInsetsCompat.CONSUMED;
            }
        });
        //Log.e("vdfsxfdssd", "Bearer HUIHufsduhqiusdfuisiuYHfd".split("Bearer ")[1]);
        setContentView(R.layout.activity_main);
        BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onReceive(Context context, Intent intent) {
                if( WebAppInterface._mediaSession != null) {
                    WebAppInterface._mediaSession.getController().getTransportControls().pause();
                    //fix debounce
                    WebAppInterface._mediaSession.getController().getTransportControls().pause();
                }
            }
        };
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        if (Build.VERSION.SDK_INT >= 34 && getApplicationInfo().targetSdkVersion >= 34) {
            registerReceiver(mNoisyReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(mNoisyReceiver, filter);
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        MediaWebView webView = findViewById(R.id.wb);
        MainActivity main = this;
        mGetContent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (result) -> {
                    if (result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (result.getResultCode() == RESULT_OK) {
                            final int takeFlags = result.getData().getFlags()
                                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            getContentResolver().takePersistableUriPermission(uri, takeFlags);
                            if (uri != null) {
                                String path = new File(uri.getPath()).getAbsolutePath();

                                if (path != null) {
                                    String filename;
                                    Cursor cursor = getContentResolver().query(uri, null, null, null, null);

                                    if (cursor == null) filename = uri.getPath();
                                    else {
                                        cursor.moveToFirst();
                                        int idx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME);
                                        filename = cursor.getString(idx);
                                        cursor.close();
                                    }

                                    String name = filename.substring(0, filename.lastIndexOf("."));
                                    String extension = filename.substring(filename.lastIndexOf(".") + 1);
                                    webView.evaluateJavascript("listeners.filePickerCallback([[`" + uri.toString() + "." + extension + "`, `" + name + "." + extension + "`]])", null);
                                }
                            } else {
                                webView.evaluateJavascript("listeners.filePickerCallback([])", null);
                            }
                        }
                    }
                });
        actualWb = webView;
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                String[] resources = request.getResources();
                for (String resource : resources) {
                    if (PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID.equals(resource)) {
                        request.grant(resources);
                        return;
                    }
                }

                super.onPermissionRequest(request);
            }
        });
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowContentAccess(true);
        final boolean[] first = {true};
        webView.setWebViewClient(new WebViewClient() {
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return WVshouldInterceptRequest(main, view, request, 0);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if(getIntent().getBooleanExtra("enableAutoPlay", false)) {
                    view.evaluateJavascript("window.enableFirstAutoPlay = true", null);
                }
                view.evaluateJavascript("if(!loaded) {" +
                        "var intev = setInterval(() => {\n" +
                        "            if(!loaded) {\n" +
                        "                console.log('Attempt registerClient')\n" +
                        "                if(typeof app != 'undefined' && app) {\n" +
                        "                    app.registerClient('Android', 'v" + BuildConfig.VERSION_NAME + "', " + BuildConfig.VERSION_CODE + ", window.boundobject, " + (BuildConfig.IS_RELEASE) + ")\n" +
                        "                    clearInterval(intev)\n" +
                        "                }\n" +
                        "            }\n" +
                        "            else {\n" +
                        "                clearInterval(intev)\n" +
                        "            }\n" +
                        "        }, 100)\n" +
                        "        app.registerClient('Android', 'v" + BuildConfig.VERSION_NAME + "', " + BuildConfig.VERSION_CODE + ", window.boundobject, " + (BuildConfig.IS_RELEASE) + ")\n" +
                        "}", null);
                super.onPageFinished(view, url);
            }

        });
        ServiceWorkerControllerCompat swController = ServiceWorkerControllerCompat.getInstance();
        swController.setServiceWorkerClient(new ServiceWorkerClientCompat() {
            @Override
            public WebResourceResponse shouldInterceptRequest(@NonNull WebResourceRequest request) {
                return WVshouldInterceptRequest(main, null, request, 0);
            }
        });
        //load();
        swController.getServiceWorkerWebSettings().setAllowContentAccess(true);
        WebSettings webViewSettings = webView.getSettings();
        webViewSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        webViewSettings.setJavaScriptEnabled(true);
        webViewSettings.setAllowUniversalAccessFromFileURLs(true);
        webViewSettings.setAllowContentAccess(true);
        webViewSettings.setAllowFileAccessFromFileURLs(true);
        webViewSettings.setDomStorageEnabled(true);
        webViewSettings.setDatabaseEnabled(true);
        File dir = getCacheDir();
        if (!dir.exists()) dir.mkdirs();
        handleWebSettings_setAppCachePath(webViewSettings, dir.getPath());
        webViewSettings.setSupportZoom(false);
        webViewSettings.setMediaPlaybackRequiresUserGesture(false);
        webViewSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webViewSettings.setBuiltInZoomControls(false);
        webView.setWebContentsDebuggingEnabled(true);
        webViewSettings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36");
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView,true);
        webView.addJavascriptInterface(new WebAppInterface(this, webView, this), "boundobject");
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEBVIEW_MEDIA_INTEGRITY_API_STATUS)) {
            // Create a configuration that disables the Media Integrity API by default
            WebViewMediaIntegrityApiStatusConfig config =
                    new WebViewMediaIntegrityApiStatusConfig.Builder(
                            WebViewMediaIntegrityApiStatusConfig.WEBVIEW_MEDIA_INTEGRITY_API_DISABLED
                    ).build();
            WebSettingsCompat.setWebViewMediaIntegrityApiStatus(webViewSettings, config);
        }
        webView.loadUrl("https://myapp/index.html");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
            }
        }
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    @Override
    public void onBackPressed() {
        WebView view = findViewById(R.id.wb);
        if(view.canGoBack()) {
            view.goBack();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if(intent.getExtras() != null && intent.getExtras().containsKey("showListen")) {
            ((MediaWebView)findViewById(R.id.wb)).evaluateJavascript("listeners.showListenViewerWindow()", null);
        }
        super.onNewIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private Semaphore semaphore2 = new Semaphore(1);

    @Override
    protected void onDestroy() {
        try {
            semaphore2.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        MainActivity.actualWb.evaluateJavascript("window.listeners.player.disconnect()", new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {
                semaphore2.release();
            }
        });
        super.onDestroy();
        //force stop
        System.exit(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        //activityManager.moveTaskToFront(popupTaskId, ActivityManager.MOVE_TASK_NO_USER_ACTION);
    }

    /**
     * use reflection to avoid compilation failure when you set compileSdk>=33
     * <p>
     *
     * @param webSettings  WebSettings
     * @param appCachePath appCachePath
     * @author androidmalin
     */
    public static void handleWebSettings_setAppCachePath(WebSettings webSettings, String appCachePath) {
        if (webSettings == null) return;
        if (appCachePath == null || appCachePath.isEmpty()) return;
        if (Build.VERSION.SDK_INT >= 33) return;
        try {
            // public abstract void setAppCachePath(String appCachePath);
            Class<?> webSettingsClazz = Class.forName("android.webkit.WebSettings");
            //noinspection JavaReflectionMemberAccess
            Method setAppCachePathMethod = webSettingsClazz.getDeclaredMethod("setAppCachePath", String.class);
            setAppCachePathMethod.setAccessible(true);
            setAppCachePathMethod.invoke(webSettings, appCachePath);
        } catch (Throwable e) {
            //ignore
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    private WebResourceResponse WVshouldInterceptRequest(MainActivity main, WebView view, WebResourceRequest request, int tryN) {
        if (request.getUrl() != null && !Adblock.isAGoodUrl(request.getUrl().toString()))
            return new WebResourceResponse("text/html", "UTF-8", new ByteArrayInputStream("<p></p>".getBytes()));
        String urlrewrite = request.getUrl().toString();
                /*CookieManager.getInstance().setAcceptCookie(true);
                CookieManager.getInstance().acceptCookie();
                CookieManager.getInstance().flush();*/
        String CACHE_APP_SCHEME = "https://mycache/";
        if (urlrewrite.startsWith(CACHE_APP_SCHEME)) {
            String newUrl = urlrewrite.replace(CACHE_APP_SCHEME, "");
            try {
                File tempFile = new File(main.getCacheDir() + "/" + newUrl);
                FileInputStream is = new FileInputStream(tempFile);
                String mimeType = Utils.getMimetypeFromUrl(newUrl);
                Map<String, String> map = new HashMap<>();
                map.put("Access-Control-Allow-Origin", "*");
                return new WebResourceResponse(mimeType, "UTF-8", 200, "0K", map, is);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        String DATA_APP_SCHEME = "https://mydata/";
        if (urlrewrite.startsWith(DATA_APP_SCHEME)) {
            String newUrl = urlrewrite.replace(DATA_APP_SCHEME, "");
            try {
                File tempFile = new File(main.getDataDir() + "/" + newUrl);
                FileInputStream is = new FileInputStream(tempFile);
                String mimeType = Utils.getMimetypeFromUrl(newUrl);
                Map<String, String> map = new HashMap<>();
                map.put("Access-Control-Allow-Origin", "*");
                return new WebResourceResponse(mimeType, "UTF-8", 200, "0K", map, is);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        String FILES_APP_SCHEME = "https://myfiles/";
        if (urlrewrite.startsWith(FILES_APP_SCHEME)) {
            String newUrl = urlrewrite.replace(FILES_APP_SCHEME, "");
            try {
                String mimeType = Utils.getMimetypeFromUrl(newUrl);
                Collection<String> hashset = new ArrayList<>(Arrays.asList(newUrl.split("\\.")));
                hashset.remove(newUrl.split("\\.")[newUrl.split("\\.").length - 1]);
                newUrl = TextUtils.join(".", hashset);
                //Log.e("sdqfdsqfqs", newUrl);
                InputStream is = getContentResolver().openInputStream(Uri.parse(newUrl));
                for(Map.Entry<String, String> header : request.getRequestHeaders().entrySet()) {
                    if(header.getKey().equals("Range")) {
                        int skip = Integer.parseInt(header.getValue().split("=")[1].split("-")[0]);
                        //Log.e("dqffqsdfsqfsd", skip + "");
                        //is.skip(skip);
                    }
                }
                //loadedAssets.put(newUrl, total.toString());
                Map<String, String> map = new HashMap<>();
                map.put("Access-Control-Allow-Origin", "*");
                //map.put("Accept-Ranges", "bytes");
                //Log.e("sqfsdqfsdqfsdq", String.valueOf(is.available()));
                map.put("Access-Control-Allow-Headers", "*");
                WebResourceResponse resp = new WebResourceResponse(mimeType, "UTF-8", is);
                resp.setResponseHeaders(map);
                return resp;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        String APP_SCHEME = "https://myapp/";
        if (urlrewrite.startsWith(APP_SCHEME)) {
            String newUrl = urlrewrite.replace(APP_SCHEME, "");
            try {
                AssetManager am = getAssets();
                InputStream is = am.open(newUrl);
                String mimeType = Utils.getMimetypeFromUrl(newUrl);
                //loadedAssets.put(newUrl, total.toString());
                return new WebResourceResponse(mimeType, "UTF-8", is);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        if(ScriptInjecter.haveBypassRequest(urlrewrite) || ((urlrewrite.contains("youtube.com") || urlrewrite.contains("google.com") || urlrewrite.contains("spotify.com") || ScriptInjecter.haveScriptForUrl(urlrewrite)) && (request.getMethod().equals("GET") || ScriptInjecter.haveInterceptAllWebRequest(urlrewrite)))) {
            try {
                //String nhtml = new getData().execute(urlrewrite).get();
                ScriptInjecter.setUrlLoaded(urlrewrite, 0);
                HttpURLConnection connection = (HttpURLConnection) (new URL(urlrewrite)).openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setInstanceFollowRedirects(false);
                connection.setRequestMethod(request.getMethod());
                String mimeType = Utils.getMimetypeFromUrl(urlrewrite);
                if(request.getMethod().toLowerCase().equals("post") && WebAppInterface.postData != null) {
                    connection.setDoOutput(true);
                    OutputStream os = connection.getOutputStream();
                    OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                    osw.write(WebAppInterface.postData);
                    osw.flush();
                    osw.close();
                    os.close();
                    WebAppInterface.postData = null;
                }
                for(Map.Entry<String, String> head : request.getRequestHeaders().entrySet()) {
                    connection.addRequestProperty(head.getKey(), head.getValue());
                    //build.addHeader(head.getKey(), head.getValue());
                }
                if(view == null) {
                    connection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36");
                }
                String cookieUrl = "https://" + request.getUrl().getHost().replace("www.", "") + "/";
                if(CookieManager.getInstance().getCookie(urlrewrite) != null) {
                    connection.setRequestProperty("Cookie", CookieManager.getInstance().getCookie(urlrewrite));
                }
                connection.connect();
                if(connection.getResponseCode() >= 300 && connection.getResponseCode() <= 399) {
                    ScriptInjecter.setUrlLoaded(urlrewrite, 1);
                    if(!ScriptInjecter.haveInterceptAllWebRequest(urlrewrite)) return null;
                    Map<String, String> respH = new HashMap<>();
                    //for(String h : resp.headers().names()) {
                    boolean newCookies = false;
                    for(Map.Entry<String, List<String>> entries : connection.getHeaderFields().entrySet()) {
                        String h = entries.getKey();
                        if(h != null) {
                            for (String val : entries.getValue()) {
                                if (!h.toLowerCase().equals("x-frame-options") && !h.toLowerCase().equals("content-security-policy-report-only") && !h.toLowerCase().equals("Cross-Origin-Opener-Policy-Report-Only".toLowerCase()) && !h.toLowerCase().equals("Cross-Origin-Resource-Policy".toLowerCase()) && !h.toLowerCase().equals("Permissions-Policy".toLowerCase()) && !h.toLowerCase().equals("Report-To".toLowerCase()) && !h.toLowerCase().equals("Content-Security-Policy".toLowerCase())) {
                                    String nVal = val;
                                    if (h.toLowerCase().equals("set-cookie")) {
                                        if (val.contains("SameSite=lax")) {
                                            nVal = val.replace("SameSite=lax", "SameSite=None; Secure; Partitioned");
                                        } else {
                                            nVal = val + "; SameSite=None; Partitioned";
                                        }
                                    }
                                    if(view == null) {
                                        newCookies = true;
                                        CookieManager.getInstance().setCookie(urlrewrite, nVal);
                                    }
                                    respH.put(h, nVal);
                                }
                            }
                        }
                        if(view == null && newCookies) {
                            CookieManager.getInstance().setAcceptCookie(true);
                            CookieManager.getInstance().acceptCookie();
                            CookieManager.getInstance().flush();
                        }
                    }
                    Map<String, String> respHrm = new HashMap<>(respH);
                    for(Map.Entry<String, String> head : respHrm.entrySet()) {
                        if(head.getKey().equalsIgnoreCase("access-control-allow-origin") ||
                                head.getKey().equalsIgnoreCase("Access-Control-Allow-Credentials")) respH.remove(head.getKey(), head.getValue());
                    }
                    String origin = request.getRequestHeaders().getOrDefault("origin", "*");
                    if(request.getRequestHeaders().containsKey("Origin")) origin = request.getRequestHeaders().get("Origin");
                    if(Objects.equals(origin, "")) origin = "*";
                    respH.put("access-control-allow-origin", origin);
                    respH.put("access-control-allow-credentials", "true");
                    String newUrl = respH.get("location") != null ? respH.get("location") : respH.get("Location");
                    respH.remove("location");
                    respH.remove("Location");
                    respH.put("X-Location-Manual", newUrl);
                    String content = "<html><head></head><body><script>location.href = '" + newUrl + "'</script></body></html>";
                    return new WebResourceResponse("text/html", "utf-8", 200, "OK", respH, new ByteArrayInputStream(content.getBytes()));
                }
                InputStream is = null;
                HashMap<String, String> overrides = ScriptInjecter.haveOverrideResponseForRequest(urlrewrite, connection.getHeaderFields());
                if(ScriptInjecter.haveScriptForUrl(urlrewrite) || overrides != null) {
                    //String nhtml = resp.body().string();
                    InputStream in = connection.getInputStream();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer)) > -1 ) {
                        baos.write(buffer, 0, len);
                    }
                    baos.flush();
                    InputStream is1 = new ByteArrayInputStream(baos.toByteArray());
                    InputStream is2 = new ByteArrayInputStream(baos.toByteArray());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is2));
                    StringBuilder html = new StringBuilder();
                    for (String line; (line = reader.readLine()) != null; ) {
                        html.append(line + "\n");
                    }
                    is2.close();
                    //
                    String output = html.toString();
                    String nhtml = output;
                    if (overrides != null) {
                        for(Map.Entry<String, String> entr : overrides.entrySet()) {
                            nhtml = nhtml.replace(entr.getKey(), entr.getValue());
                        }
                        is = new ByteArrayInputStream(nhtml.getBytes(StandardCharsets.UTF_8));
                    }
                    if(nhtml.contains("</body>")) {
                        for (String s : ScriptInjecter.getScriptsForUrl(urlrewrite)) {
                            nhtml = nhtml.replace("</body>", "<script>" +
                                    "" + s + "; " +
                                    "console.log(location.href);" +
                                    "</script></body>");
                        }
                        //if(nhtml.contains("<body>")) Log.e("fdsqfsq", nhtml);
                        is = new ByteArrayInputStream(nhtml.getBytes(StandardCharsets.UTF_8));
                    }
                    if(is == null) {
                        is = is1;
                    }
                }
                else {
                    //is = Objects.requireNonNull(resp.body()).byteStream();
                    InputStream in = connection.getInputStream();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer)) > -1 ) {
                        baos.write(buffer, 0, len);
                    }
                    baos.flush();
                    is = new ByteArrayInputStream(baos.toByteArray());
                }
                Map<String, String> respH = new HashMap<>();
                //for(String h : resp.headers().names()) {
                boolean newCookies = false;
                for(Map.Entry<String, List<String>> entries : connection.getHeaderFields().entrySet()) {
                    String h = entries.getKey();
                    if(h != null) {
                        for (String val : entries.getValue()) {
                            if (!h.toLowerCase().equals("x-frame-options") && !h.toLowerCase().equals("content-security-policy-report-only") && !h.toLowerCase().equals("Cross-Origin-Opener-Policy-Report-Only".toLowerCase()) && !h.toLowerCase().equals("Cross-Origin-Resource-Policy".toLowerCase()) && !h.toLowerCase().equals("Permissions-Policy".toLowerCase()) && !h.toLowerCase().equals("Report-To".toLowerCase()) && !h.toLowerCase().equals("Content-Security-Policy".toLowerCase())) {
                                String nVal = val;
                                if (h.toLowerCase().equals("set-cookie")) {
                                    if (val.contains("SameSite=lax")) {
                                        nVal = val.replace("SameSite=lax", "SameSite=None; Secure; Partitioned");
                                    } else {
                                        nVal = val + "; SameSite=None; Partitioned";
                                    }
                                    Log.d(TAG, "shouldInterceptRequest: " + nVal);
                                    if(view == null) {
                                        newCookies = true;
                                        CookieManager.getInstance().setCookie(urlrewrite, nVal);
                                    }
                                }
                                respH.put(h, nVal);
                            }
                        }
                    }
                    if(view == null && newCookies) {
                        CookieManager.getInstance().setAcceptCookie(true);
                        CookieManager.getInstance().acceptCookie();
                        CookieManager.getInstance().flush();
                    }
                }
                Map<String, String> respHrm = new HashMap<>();
                for(Map.Entry<String, String> head : respH.entrySet()) {
                    respHrm.put(head.getKey(), head.getValue());
                }
                for(Map.Entry<String, String> head : respHrm.entrySet()) {
                    if(head.getKey().toLowerCase().equals("access-control-allow-origin")) respH.remove(head.getKey(), head.getValue());
                }
                respH.put("access-control-allow-origin", "*");
                ScriptInjecter.setUrlLoaded(urlrewrite, 1);
                //return new WebResourceResponse(mimeType, "UTF-8", resp.code(), !resp.message().equals("") ? resp.message() : "OK", respH, is);
                return new WebResourceResponse(mimeType, "UTF-8", connection.getResponseCode(), connection.getResponseMessage(), respH, is);
            }
            catch (SocketException e) {
                e.printStackTrace();
                if(tryN < 3) {
                    Log.e("shouldInterceptRequest", "retrying");
                    return WVshouldInterceptRequest(main, view, request, tryN + 1);
                }
                else {
                    Log.e("shouldInterceptRequest", "can't retry after 3 tries " + urlrewrite);
                    ScriptInjecter.setUrlLoaded(urlrewrite, 2);
                }
                return null;
            }
            catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }
}