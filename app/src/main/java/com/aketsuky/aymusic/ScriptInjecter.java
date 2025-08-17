package com.aketsuky.aymusic;

import android.util.Log;
import android.webkit.WebView;

import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScriptInjecter {
    static HashMap<String, String> map = new HashMap<>();
    static ArrayList<JSONObject> overList = new ArrayList<>();
    static HashMap<String, Integer> urlsLoaded = new HashMap<>();

    public static void addScript(String url, String script) {
        if(!map.containsKey(url)) {
            map.put(url, script);
            urlsLoaded.put(url, 0);
        }
        else {
            map.replace(url, script);
            urlsLoaded.replace(url, 0);
        }
    }

    public static int getUrlStatus(String url) {
        return urlsLoaded.getOrDefault(url, 0);
    }

    public static void setUrlLoaded(String url, int status) {
        //status: 0 - not loaded, 1 - loaded, 2 - failed
        if(!urlsLoaded.containsKey(url)) {
            urlsLoaded.put(url, status);
        }
        else {
            urlsLoaded.replace(url, status);
        }
    }

    public static void resetUrlsLoaded() {
        for(Map.Entry<String, Integer> u : urlsLoaded.entrySet()) {
            urlsLoaded.replace(u.getKey(), 0);
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    public static void addOverrideResponse(String json) throws JSONException {
        JSONArray arr = new JSONArray(json);
        for(int j = 0; j < arr.length(); j++) {
            JSONObject obj = arr.getJSONObject(j);
            JSONObject urlInfo = obj.getJSONObject("url");
            JSONArray plats = obj.getJSONArray("platforms");
            for (int i = 0; i < plats.length(); i++) {
                if (plats.getString(i).equals("Android")) {
                    WebAppInterface.bpWR.put(urlInfo.getString("url"), urlInfo.getBoolean("includes"));
                    if(!overList.contains(obj)) {
                        overList.add(obj);
                        break;
                    }
                }
            }
        }
    }

    public static HashMap<String, String> haveOverrideResponseForRequest(String url, Map<String, List<String>> headers) {
        try {
            for(JSONObject o : overList) {
                if(o.getJSONArray("headers").length() > 0) {
                    for (int hi = 0; hi < o.getJSONArray("headers").length(); hi++) {
                        String headerName = o.getJSONArray("headers").getJSONObject(hi).getString("name");
                        String headerValue = o.getJSONArray("headers").getJSONObject(hi).getString("value");
                        boolean headerIncludes = o.getJSONArray("headers").getJSONObject(hi).getBoolean("includes");
                        if ((o.getJSONObject("url").getBoolean("includes") && url.contains(o.getJSONObject("url").getString("url")) || url.equals(o.getJSONObject("url").getString("url")))
                                && headers.containsKey(headerName)
                                && headers.get(headerName).size() >= 1
                                && ((headerIncludes && headers.get(headerName).get(0).contains(headerValue)) || headers.get(headerName).get(0).equals(headerValue))) {
                            HashMap<String, String> ret = new HashMap<>();
                            for (int i = 0; i < o.getJSONArray("overrides").length(); i++) {
                                ret.put(o.getJSONArray("overrides").getJSONObject(i).getString("search"), o.getJSONArray("overrides").getJSONObject(i).getString("replace"));
                            }
                            return ret;
                        }
                    }
                }
                else {
                    if ((o.getJSONObject("url").getBoolean("includes") && url.contains(o.getJSONObject("url").getString("url"))) || url.equals(o.getJSONObject("url").getString("url"))) {
                        HashMap<String, String> ret = new HashMap<>();
                        for (int i = 0; i < o.getJSONArray("overrides").length(); i++) {
                            ret.put(o.getJSONArray("overrides").getJSONObject(i).getString("search"), o.getJSONArray("overrides").getJSONObject(i).getString("replace"));
                        }
                        return ret;
                    }
                }
            }
            return null;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean haveScriptForUrl(String url) {
        try {
            String charset = StandardCharsets.UTF_8.toString();
            String urlEncoded = URLEncoder.encode(URLDecoder.decode(url, charset), charset);
            for (Map.Entry<String, String> u : map.entrySet()) {
                String urlEncoded2 = URLEncoder.encode(URLDecoder.decode(u.getKey(), charset), charset);
                if (urlEncoded.contains(urlEncoded2)) {
                    return true;
                }
            }
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> getScriptsForUrl(String url) throws UnsupportedEncodingException {
        List<String> strs = new ArrayList<>();
        String charset = StandardCharsets.UTF_8.toString();
        String urlEncoded = URLEncoder.encode(URLDecoder.decode(url, charset), charset);
        for(Map.Entry<String, String> u : map.entrySet()) {
            String urlEncoded2 = URLEncoder.encode(URLDecoder.decode(u.getKey(), charset), charset);
            if(urlEncoded.contains(urlEncoded2)) {
                strs.add(u.getValue().replace("'app://root'", "'https://myapp'"));
            }
        }
        return strs;
    }

    @OptIn(markerClass = UnstableApi.class)
    public static boolean haveBypassRequest(String url) {
        for(Map.Entry<String, Boolean> u : WebAppInterface.bpWR.entrySet()) {
            if(u.getValue()) {
                if(url.contains(u.getKey())) return true;
            }
            else {
                if(url.equals(u.getKey())) return true;
            }
        }
        return false;
    }

    @OptIn(markerClass = UnstableApi.class)
    public static boolean haveInterceptAllWebRequest(String url) {
        for(Map.Entry<String, Boolean> u : WebAppInterface.interceptAll.entrySet()) {
            if(u.getValue()) {
                if(url.contains(u.getKey())) return true;
            }
            else {
                if(url.equals(u.getKey())) return true;
            }
        }
        return false;
    }
}
