package com.aketsuky.aymusic;

import android.util.Log;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScriptInjecter {
    static HashMap<String, String> map = new HashMap<>();
    static ArrayList<JSONObject> overList = new ArrayList<>();

    public static void addScript(String url, String script) {
        if(!map.containsKey(url)) {
            map.put(url, script);
        }
        else
            map.replace(url, script);
    }

    public static void addOverrideResponse(String json) throws JSONException {
        JSONArray arr = new JSONArray(json);
        for(int j = 0; j < arr.length(); j++) {
            JSONObject obj = arr.getJSONObject(j);
            JSONObject urlInfo = obj.getJSONObject("url");
            JSONArray plats = obj.getJSONArray("platforms");
            for (int i = 0; i < plats.length(); i++) {
                if (plats.getString(i).equals("Android")) {
                    for (JSONObject o : overList) {
                        if (o.getJSONObject("url").getString("url").equals(urlInfo.getString("url")))
                            return;
                    }
                    overList.add(obj);
                }
            }
        }
    }

    public static HashMap<String, String> haveOverrideResponseForRequest(String url, Map<String, List<String>> headers) {
        try {
            for(JSONObject o : overList) {
                for (int hi = 0; hi < o.getJSONArray("headers").length(); hi++) {
                    String headerName = o.getJSONArray("headers").getJSONObject(hi).getString("name");
                    String headerValue = o.getJSONArray("headers").getJSONObject(hi).getString("value");
                    boolean headerIncludes = o.getJSONArray("headers").getJSONObject(hi).getBoolean("includes");
                    if ((o.getJSONObject("url").getBoolean("includes") && url.contains(o.getJSONObject("url").getString("url")) || url.equals(o.getJSONObject("url").getString("url")))
                            && headers.containsKey(headerName)
                            && headers.get(headerName).size() == 1
                            && (headerIncludes && headers.get(headerName).get(0).contains(headerValue) || headers.get(headerName).get(0).equals(headerValue))) {
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
            for (Map.Entry<String, String> u : map.entrySet()) {
                if (url.contains(u.getKey())) return true;
            }
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> getScriptsForUrl(String url) {
        List<String> strs = new ArrayList<>();
        for(Map.Entry<String, String> u : map.entrySet()) {
            if(url.contains(u.getKey())) strs.add(u.getValue().replace("'app://root'", "'https://myapp'"));
        }
        return strs;
    }
}
