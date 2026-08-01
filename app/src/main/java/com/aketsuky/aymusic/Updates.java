package com.aketsuky.aymusic;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.webkit.WebView;

import androidx.core.content.FileProvider;

import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;

public class Updates {
    public static String servUrl = "";

    public static void searchUpdates(Context mContext, WebView view, MainActivity mainActivity)
    {
        String jsonUrl;
        //if Android 10 or more
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q)
            jsonUrl = Updates.servUrl + "/dl/AyMusic/update_android_10.json";
        else {
            jsonUrl = Updates.servUrl + "/dl/AyMusic/update_android.json";
        }
        Handler mainHandler = new Handler(mContext.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                view.evaluateJavascript("updateCallBack({\n" +
                        "                    step: 0,\n" +
                        "                    file: '" + jsonUrl + "',\n" +
                        "                    cur: 0,\n" +
                        "                    max: 100\n" +
                        "                })", null);
            } // This is your code
        };
        mainHandler.post(myRunnable);
        new getData() {
            @Override
            protected void onPostExecute(String s) {
                try {
                    if(s != null) {
                        JSONObject json = new JSONObject(s);
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    view.evaluateJavascript("updateCallBack({\n" +
                                            "                    step: 0,\n" +
                                            "                    file: '" + jsonUrl + "',\n" +
                                            "                    cur: 50,\n" +
                                            "                    max: 100\n" +
                                            "                })", null);
                                    String info = json.getString("info");
                                    int code = json.getInt("versionCode");
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
                                } catch (Exception e) {
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
                    }
                    else {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                view.evaluateJavascript("updateCallBack({\n" +
                                        "                    step: -2,\n" +
                                        "                    file: null,\n" +
                                        "                    cur: 0,\n" +
                                        "                    max: 1,\n" +
                                        "                    error: `The update server is offline. Please wait.`" +
                                        "                })", null);
                            } // This is your code
                        });
                    }
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
                                    "                    error: `" + s.replace("<", "") + "`" +
                                    "                })", null);
                        } // This is your code
                    });
                }
                super.onPostExecute(s);
            }
        }.execute(jsonUrl);
    }
}
