package com.aketsuky.aymusic;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;
import androidx.webkit.WebViewMediaIntegrityApiStatusConfig;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.MimeTypeMap;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WebViewPopup extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        String baseUrl = extras.getString("baseUrl");
        setContentView(R.layout.activity_web_view_popup);
        WebView webView = findViewById(R.id.webview);
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
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                CookieManager.getInstance().setAcceptCookie(true);
                CookieManager.getInstance().acceptCookie();
                CookieManager.getInstance().flush();
                String closeUrl = extras.getString("closeUrl");
                boolean filterByInclude = extras.getBoolean("filterByInclude");
                boolean test = url.equals(closeUrl);
                if(filterByInclude) test = url.contains(closeUrl);
                if (test) {
                    Handler mainHandler = new Handler(getMainLooper());
                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            webView.destroy();
                            finish();
                        } // This is your code
                    };
                    mainHandler.post(myRunnable);
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if(url.contains("google"))
                    view.getSettings().setUserAgentString("Chrome");
                else {
                    String ua = view.getSettings().getUserAgentString();
                    Pattern pattern = Pattern.compile("Chrome/([0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+)");
                    Matcher matcher = pattern.matcher(ua);
                    if (matcher.find()) {
                        String chromeVersion = matcher.group(1);
                        Log.i("UA", "Chrome version: " + chromeVersion);
                        view.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/" + chromeVersion + " Safari/537.36");
                    } else {
                        Log.e("UA", "Chrome version not found.");
                    }
                }
                //super.onPageStarted(view, url, favicon);
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
        webViewSettings.setDatabaseEnabled(true);
        webViewSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webViewSettings.setMediaPlaybackRequiresUserGesture(false);
        webViewSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView,true);
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEBVIEW_MEDIA_INTEGRITY_API_STATUS)) {
            // Create a configuration that disables the Media Integrity API by default
            WebViewMediaIntegrityApiStatusConfig config =
                    new WebViewMediaIntegrityApiStatusConfig.Builder(
                            WebViewMediaIntegrityApiStatusConfig.WEBVIEW_MEDIA_INTEGRITY_API_DISABLED
                    ).build();
            WebSettingsCompat.setWebViewMediaIntegrityApiStatus(webViewSettings, config);
        }
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.loadUrl(baseUrl);
    }

    @Override
    public void onBackPressed() {
        WebView view = findViewById(R.id.webview);
        if(view.canGoBack()) {
            view.goBack();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            WebView webView = findViewById(R.id.webview);
            webView.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}