package com.aketsuky.aymusic;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.MimeTypeMap;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Map<String, String> loadedAssets = new HashMap<>();
    static MediaWebView actualWb;
    ActivityResultLauncher<Intent> mGetContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.e("vdfsxfdssd", "Bearer HUIHufsduhqiusdfuisiuYHfd".split("Bearer ")[1]);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
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
                                    webView.evaluateJavascript("listeners.filePickerCallback([`" + uri.toString() + "." + extension + "`])", null);
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
                for (int i = 0; i < resources.length; i++) {
                    if (PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID.equals(resources[i])) {
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
            @Override
            public void onPageFinished(WebView view, String url) {
                /*Log.e("fdfsqdqfsdfsdq", url);
                CookieManager.getInstance().setAcceptCookie(true);
                CookieManager.getInstance().acceptCookie();
                CookieManager.getInstance().flush();*/
                view.evaluateJavascript("var intev = setInterval(() => {\n" +
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
                        "        app.registerClient('Android', 'v" + BuildConfig.VERSION_NAME + "', " + BuildConfig.VERSION_CODE + ", window.boundobject, " + (BuildConfig.IS_RELEASE) + ")", null);
                super.onPageFinished(view, url);
            }

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
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                /*CookieManager.getInstance().setAcceptCookie(true);
                CookieManager.getInstance().acceptCookie();
                CookieManager.getInstance().flush();*/
                String CACHE_APP_SCHEME = "https://mycache/";
                if (request.getUrl().toString().startsWith(CACHE_APP_SCHEME)) {
                    String newUrl = request.getUrl().toString().replace(CACHE_APP_SCHEME, "");
                    try {
                        File tempFile = new File(main.getCacheDir() + "/" + newUrl);
                        FileInputStream is = new FileInputStream(tempFile);
                        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(newUrl));
                        Map<String, String> map = new HashMap<>();
                        map.put("Access-Control-Allow-Origin", "*");
                        return new WebResourceResponse(mimeType, "UTF-8", 200, "0K", map, is);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return super.shouldInterceptRequest(view, request);
                    }
                }
                String DATA_APP_SCHEME = "https://mydata/";
                if (request.getUrl().toString().startsWith(DATA_APP_SCHEME)) {
                    String newUrl = request.getUrl().toString().replace(DATA_APP_SCHEME, "");
                    try {
                        File tempFile = new File(main.getDataDir() + "/" + newUrl);
                        FileInputStream is = new FileInputStream(tempFile);
                        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(newUrl));
                        Map<String, String> map = new HashMap<>();
                        map.put("Access-Control-Allow-Origin", "*");
                        return new WebResourceResponse(mimeType, "UTF-8", 200, "0K", map, is);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return super.shouldInterceptRequest(view, request);
                    }
                }
                String FILES_APP_SCHEME = "https://myfiles/";
                if (request.getUrl().toString().startsWith(FILES_APP_SCHEME)) {
                    String newUrl = request.getUrl().toString().replace(FILES_APP_SCHEME, "");
                    try {
                        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(newUrl));
                        Collection<String> hashset = new ArrayList<>(Arrays.asList(newUrl.split("\\.")));
                        hashset.remove(newUrl.split("\\.")[newUrl.split("\\.").length - 1]);
                        newUrl = TextUtils.join(".", hashset);
                        Log.e("sdqfdsqfqs", newUrl);
                        InputStream is = getContentResolver().openInputStream(Uri.parse(newUrl));
                        for(Map.Entry<String, String> header : request.getRequestHeaders().entrySet()) {
                            if(header.getKey().equals("Range")) {
                                int skip = Integer.parseInt(header.getValue().split("=")[1].split("-")[0]);
                                Log.e("dqffqsdfsqfsd", skip + "");
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
                        return super.shouldInterceptRequest(view, request);
                    }
                }
                String APP_SCHEME = "https://myapp/";
                if (request.getUrl().toString().startsWith(APP_SCHEME)) {
                    String newUrl = request.getUrl().toString().replace(APP_SCHEME, "");
                    try {
                        AssetManager am = getAssets();
                        InputStream is = am.open(newUrl);
                        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(newUrl));
                        //loadedAssets.put(newUrl, total.toString());
                        return new WebResourceResponse(mimeType, "UTF-8", is);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return super.shouldInterceptRequest(view, request);
                    }
                }
                if((request.getUrl().toString().contains("youtube.com") || request.getUrl().toString().contains("spotify.com") || ScriptInjecter.haveScriptForUrl(request.getUrl().toString())) && request.getMethod().equals("GET")) {
                    try {
                        //String nhtml = new getData().execute(request.getUrl().toString()).get();
                        HttpURLConnection connection = (HttpURLConnection) (new URL(request.getUrl().toString())).openConnection();
                        connection.setConnectTimeout(5000);
                        connection.setReadTimeout(5000);
                        connection.setInstanceFollowRedirects(false);
                        for(Map.Entry<String, String> head : request.getRequestHeaders().entrySet()) {
                            connection.addRequestProperty(head.getKey(), head.getValue());
                            //build.addHeader(head.getKey(), head.getValue());
                        }
                        if(CookieManager.getInstance().getCookie(request.getUrl().toString()) != null) {
                            for (HttpCookie cookie : HttpCookie.parse(CookieManager.getInstance().getCookie(request.getUrl().toString()))) {
                                connection.addRequestProperty("Cookie", cookie.toString());
                            }
                        }
                        connection.connect();
                        if(connection.getResponseCode() >= 300 && connection.getResponseCode() <= 399)
                            return null;
                        InputStream is = null;
                        if(ScriptInjecter.haveScriptForUrl(request.getUrl().toString())) {
                            //String nhtml = resp.body().string();
                            InputStream in = connection.getInputStream();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                            StringBuilder html = new StringBuilder();
                            for (String line; (line = reader.readLine()) != null; ) {
                                html.append(line + "\n");
                            }
                            in.close();

                            //
                            String output = html.toString();
                            String nhtml = output;
                            for(String s : ScriptInjecter.getScriptsForUrl(request.getUrl().toString())) {
                                nhtml = nhtml.replace("</body>", "<script>" + s +"; console.log(location.href)</script></body>");
                            }
                            //if(nhtml.contains("<body>")) Log.e("fdsqfsq", nhtml);
                            is = new ByteArrayInputStream(nhtml.getBytes(StandardCharsets.UTF_8));
                        }
                        else if(request.getUrl().toString().contains("spotify.com")) {
                            //String nhtml = resp.body().string();
                            InputStream in = connection.getInputStream();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                            StringBuilder html = new StringBuilder();
                            for (String line; (line = reader.readLine()) != null; ) {
                                html.append(line + "\n");
                            }
                            in.close();

                            //
                            String output = html.toString();
                            String nhtml = output;
                            nhtml = nhtml.replace("<head>", "<head><script>" + "setInterval(() => {" +
                                    "var _scr = {};\n" +
                                    "        for (const key in screen) {\n" +
                                    "            switch (key) {\n" +
                                    "                case \"width\":\n" +
                                    "                    _scr[key] = 1080;\n" +
                                    "                    break;\n" +
                                    "                case \"height\":\n" +
                                    "                    _scr[key] = 1920;\n" +
                                    "                    break;\n" +
                                    "                default:\n" +
                                    "                    _scr[key] = screen[key];\n" +
                                    "                    break;\n" +
                                    "            }\n" +
                                    "        }\n" +
                                    "        window.screen = _scr;" +
                                    "}, 1)" +"; console.log('gfusigusgoiudjdsgjd')</script>");
                            //if(nhtml.contains("<body>")) Log.e("fdsqfsq", nhtml);
                            is = new ByteArrayInputStream(nhtml.getBytes(StandardCharsets.UTF_8));
                        }
                        else {
                            //is = Objects.requireNonNull(resp.body()).byteStream();
                            is = connection.getInputStream();;
                        }
                        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(request.getUrl().toString()));
                        Map<String, String> respH = new HashMap<>();
                        //for(String h : resp.headers().names()) {
                        for(Map.Entry<String, List<String>> entries : connection.getHeaderFields().entrySet()) {
                            String h = entries.getKey();
                            if(h != null) {
                                for (String val : entries.getValue()) {
                                    if (request.getUrl().toString().contains("youtube.com") || request.getUrl().toString().contains("spotify.com")) {
                                        if (!h.toLowerCase().equals("x-frame-options") && !h.toLowerCase().equals("content-security-policy-report-only")
                                                && !h.toLowerCase().equals("Cross-Origin-Opener-Policy-Report-Only".toLowerCase())
                                                && !h.toLowerCase().equals("Permissions-Policy".toLowerCase())
                                                && !h.toLowerCase().equals("Report-To".toLowerCase())
                                                //&& !h.toLowerCase().equals("Content-Security-Policy".toLowerCase())) respH.put(h, resp.header(h));
                                                && !h.toLowerCase().equals("Content-Security-Policy".toLowerCase()))
                                            respH.put(h, val);
                                    } else {
                                        //respH.put(h, resp.header(h));
                                        respH.put(h, val);
                                    }
                                }
                            }
                        }
                        if(!respH.containsKey("access-control-allow-origin") && !respH.containsKey("Access-Control-Allow-Origin")) {
                            respH.put("access-control-allow-origin", "*");
                        }
                        //return new WebResourceResponse(mimeType, "UTF-8", resp.code(), !resp.message().equals("") ? resp.message() : "OK", respH, is);
                        return new WebResourceResponse(mimeType, "UTF-8", connection.getResponseCode(), connection.getResponseMessage(), respH, is);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return super.shouldInterceptRequest(view, request);
                    }
                }
                return super.shouldInterceptRequest(view, request);
            }
        });
        //load();
        WebSettings webViewSettings = webView.getSettings();
        webViewSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        webViewSettings.setJavaScriptEnabled(true);
        webViewSettings.setAllowUniversalAccessFromFileURLs(true);
        webViewSettings.setAllowContentAccess(true);
        webViewSettings.setAllowFileAccessFromFileURLs(true);
        webViewSettings.setDomStorageEnabled(true);
        webViewSettings.setAppCacheEnabled(true);
        File dir = getCacheDir();
        if (!dir.exists()) dir.mkdirs();
        webView.getSettings().setAppCachePath(dir.getPath());
        webViewSettings.setSupportZoom(false);
        webViewSettings.setMediaPlaybackRequiresUserGesture(false);
        webViewSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webViewSettings.setBuiltInZoomControls(false);
        webView.setWebContentsDebuggingEnabled(true);
        webViewSettings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.5735.106 Safari/537.36");
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView,true);
        webView.addJavascriptInterface(new WebAppInterface(this, webView, this), "boundobject");
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.loadUrl("https://myapp/index.html");

        //background
        WebView webView2 = findViewById(R.id.bgwb);
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
                //view.stopLoading();
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                for(Map.Entry<String, String> head : request.getRequestHeaders().entrySet()) {
                    if(request.getUrl().toString().contains("spotify.com") && head.getKey().toLowerCase().equals("authorization".toLowerCase())) {
                        Log.e("fdqfsdfsdq", head.getValue());
                        WebAppInterface.clientsToken.put("Spotify", head.getValue().split("Bearer ")[1]);
                        Handler mainHandler = new Handler(getMainLooper());

                        Runnable myRunnable = new Runnable() {
                            @Override
                            public void run() {
                                view.stopLoading();
                                view.loadUrl("about:blank");
                                view.getSettings().setJavaScriptEnabled(false);
                            } // This is your code
                        };
                        mainHandler.post(myRunnable);
                    }
                }
                return super.shouldInterceptRequest(view, request);
            }
        });
        WebSettings webViewSettings2 = webView2.getSettings();
        webViewSettings2.setJavaScriptCanOpenWindowsAutomatically(false);
        webViewSettings2.setJavaScriptEnabled(false);
        webViewSettings2.setAllowUniversalAccessFromFileURLs(true);
        webViewSettings2.setAllowContentAccess(true);
        webViewSettings2.setAllowFileAccessFromFileURLs(true);
        webViewSettings2.setDomStorageEnabled(true);
        webViewSettings2.setAppCacheEnabled(true);
        webViewSettings2.setMediaPlaybackRequiresUserGesture(false);
        webViewSettings2.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.5735.106 Safari/537.36");
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView2,true);
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
}