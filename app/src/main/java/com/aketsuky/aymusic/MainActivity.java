package com.aketsuky.aymusic;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.session.PlaybackState;
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
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

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
import java.util.concurrent.Semaphore;

public class MainActivity extends AppCompatActivity {

    Map<String, String> loadedAssets = new HashMap<>();
    static MediaWebView actualWb;
    ActivityResultLauncher<Intent> mGetContent;
    AudioManager.OnAudioFocusChangeListener amOn;

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
                if (request.getUrl() != null && !Adblock.isAGoodUrl(request.getUrl().toString()))
                    return new WebResourceResponse("text/html", "UTF-8", new ByteArrayInputStream("<p></p>".getBytes()));
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
                if((WebAppInterface.bpWR.contains(request.getUrl().toString()) || request.getUrl().toString().contains("youtube.com") || request.getUrl().toString().contains("google.com") || request.getUrl().toString().contains("spotify.com") || ScriptInjecter.haveScriptForUrl(request.getUrl().toString())) && (request.getMethod().equals("GET") || WebAppInterface.interceptAll.contains(request.getUrl().toString()))) {
                    try {
                        //String nhtml = new getData().execute(request.getUrl().toString()).get();
                        HttpURLConnection connection = (HttpURLConnection) (new URL(request.getUrl().toString())).openConnection();
                        connection.setConnectTimeout(5000);
                        connection.setReadTimeout(5000);
                        connection.setInstanceFollowRedirects(false);
                        connection.setRequestMethod(request.getMethod());
                        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(request.getUrl().toString()));
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
                        String cookieUrl = "https://" + request.getUrl().getHost().replace("www.", "") + "/";
                        if(CookieManager.getInstance().getCookie(cookieUrl) != null) {
                            for (String cookie : CookieManager.getInstance().getCookie(cookieUrl).split("; ")) {
                                connection.addRequestProperty("Cookie", cookie);
                                //Log.e("fdsfsdfsdfsdfs",  cookieUrl + " " + cookie);
                            }
                        }
                        connection.connect();
                        if(connection.getResponseCode() >= 300 && connection.getResponseCode() <= 399) {
                            if(!WebAppInterface.interceptAll.contains(request.getUrl().toString())) return null;
                            Map<String, String> respH = new HashMap<>();
                            //for(String h : resp.headers().names()) {
                            for(Map.Entry<String, List<String>> entries : connection.getHeaderFields().entrySet()) {
                                String h = entries.getKey();
                                if(h != null) {
                                    for (String val : entries.getValue()) {
                                        if (!h.toLowerCase().equals("x-frame-options") && !h.toLowerCase().equals("content-security-policy-report-only") && !h.toLowerCase().equals("Cross-Origin-Opener-Policy-Report-Only".toLowerCase()) && !h.toLowerCase().equals("Cross-Origin-Resource-Policy".toLowerCase()) && !h.toLowerCase().equals("Permissions-Policy".toLowerCase()) && !h.toLowerCase().equals("Report-To".toLowerCase()) && !h.toLowerCase().equals("Content-Security-Policy".toLowerCase())) {
                                            String nVal = val;
                                            if (h.toLowerCase().equals("set-cookie")) {
                                                if (val.contains("SameSite=lax")) {
                                                    nVal = val.replace("SameSite=lax", "SameSite=None; Secure");
                                                } else {
                                                    nVal = val + "; SameSite=None";
                                                }
                                            }
                                            respH.put(h, nVal);
                                        }
                                    }
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
                            String newUrl = respH.get("location") != null ? respH.get("location") : respH.get("Location");
                            respH.remove("location");
                            respH.remove("Location");
                            String content = "<script>location.href = '" + newUrl + "'</script>";
                            return new WebResourceResponse("text/html", "utf-8", 200, "OK", respH, new ByteArrayInputStream(content.getBytes()));
                        }
                        InputStream is = null;
                        if(ScriptInjecter.haveScriptForUrl(request.getUrl().toString())) {
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
                            if(nhtml.contains("</body>")) {
                                for (String s : ScriptInjecter.getScriptsForUrl(request.getUrl().toString())) {
                                    if(WebAppInterface.interceptAll.contains(request.getUrl().toString())) {
                                        nhtml = nhtml.replace("</body>", "<script>" +
                                                "document.querySelectorAll(\"form\").forEach(x => {\n" +
                                                "   let form = x;\n" +
                                                "   console.log('found', form);\n" +
                                                "   form.onsubmit = () => {\n" +
                                                "      var kvpairs = [];\n" +
                                                "      for (var i = 0; i < form.elements.length; i++) {\n" +
                                                "         var e = form.elements[i];\n" +
                                                "         kvpairs.push(encodeURIComponent(e.name) + \"=\" + encodeURIComponent(e.value));\n" +
                                                "      }\n" +
                                                "      var queryString = kvpairs.join(\"&\");\n" +
                                                "      console.log(queryString);\n" +
                                                "      boundobject.setPostData(queryString, \"\");\n" +
                                                "      return true;\n" +
                                                "   }\n" +
                                                "});\n" +
                                                "" + s + "; " +
                                                "console.log(location.href);" +
                                                "</script></body>");
                                    }
                                    else {
                                        nhtml = nhtml.replace("</body>", "<script>" +
                                                "" + s + "; " +
                                                "console.log(location.href);" +
                                                "</script></body>");
                                    }
                                }
                                //if(nhtml.contains("<body>")) Log.e("fdsqfsq", nhtml);
                                is = new ByteArrayInputStream(nhtml.getBytes(StandardCharsets.UTF_8));
                            }
                            else {
                                is = is1;
                            }
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
                        for(Map.Entry<String, List<String>> entries : connection.getHeaderFields().entrySet()) {
                            String h = entries.getKey();
                            if(h != null) {
                                for (String val : entries.getValue()) {
                                    if (!h.toLowerCase().equals("x-frame-options") && !h.toLowerCase().equals("content-security-policy-report-only") && !h.toLowerCase().equals("Cross-Origin-Opener-Policy-Report-Only".toLowerCase()) && !h.toLowerCase().equals("Cross-Origin-Resource-Policy".toLowerCase()) && !h.toLowerCase().equals("Permissions-Policy".toLowerCase()) && !h.toLowerCase().equals("Report-To".toLowerCase()) && !h.toLowerCase().equals("Content-Security-Policy".toLowerCase())) {
                                        String nVal = val;
                                        if (h.toLowerCase().equals("set-cookie")) {
                                            if (val.contains("SameSite=lax")) {
                                                nVal = val.replace("SameSite=lax", "SameSite=None; Secure");
                                            } else {
                                                nVal = val + "; SameSite=None";
                                            }
                                        }
                                        respH.put(h, nVal);
                                    }
                                }
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
                if (request.getUrl().toString().contains("https://api-auth.soundcloud.com/oauth/authorize")) {
                    Log.e("fdqfsdfsdq", request.getUrl().getQueryParameter("client_id"));
                    WebAppInterface.clientsToken.put("Soundcloud", request.getUrl().getQueryParameter("client_id"));
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
        amOn = new AudioManager.OnAudioFocusChangeListener() {
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
        };
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

    private Semaphore semaphore = new Semaphore(1);

    @Override
    protected void onPause() {
        if(MyService.instance != null) {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            MainActivity.actualWb.evaluateJavascript("listeners.player.setVolume(0); listeners.player.play();", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String s) {
                    semaphore.release();
                }
            });
            super.onPause();
            AudioManager am = (AudioManager) actualWb.getContext().getSystemService(Context.AUDIO_SERVICE);
            AudioFocusRequest afr = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                afr = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(
                                new AudioAttributes.Builder()
                                        .setUsage(AudioAttributes.USAGE_MEDIA)
                                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                        .build()
                        )
                        .setOnAudioFocusChangeListener(amOn)
                        .build();
            }
            //int aa = am.requestAudioFocus(afr,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
            int aa = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                aa = am.requestAudioFocus(afr);
            }
            else aa = am.requestAudioFocus(null ,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
            if (aa == AudioManager.AUDIOFOCUS_REQUEST_GRANTED && WebAppInterface._mediaSession.getController().getPlaybackState().getState() == PlaybackState.STATE_PLAYING) {
                WebAppInterface._mediaSession.getController().getTransportControls().play();
            }
            MainActivity.actualWb.evaluateJavascript("listeners.player.setVolume(100);", null);
        }
        else {
            super.onPause();
        }
        //WebAppInterface._mediaSession.getController().getTransportControls().play();
        /*MediaWebView.changeVisibility = true;
        WebView view = findViewById(R.id.wb);
        view.evaluateJavascript("window.listeners.changeDocumentVisibility(false)", null);
        view.setVisibility(View.GONE);
        MediaWebView.changeVisibility = false;*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*MediaWebView.changeVisibility = true;
        WebView view = findViewById(R.id.wb);
        view.evaluateJavascript("window.listeners.changeDocumentVisibility(true)", null);
        view.setVisibility(View.VISIBLE);
        MediaWebView.changeVisibility = false;*/
    }
}