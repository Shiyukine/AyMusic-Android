package com.aketsuky.aymusic;

import android.util.Log;
import android.webkit.WebView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScriptInjecter {
    static HashMap<String, String> map = new HashMap<>();

    public static void addScript(String url, String script) {
        if(!map.containsKey(url)) {
            map.put(url, script);
        }
    }

    public static boolean haveScriptForUrl(String url) {
        for(Map.Entry<String, String> u : map.entrySet()) {
            if(url.contains(u.getKey())) return true;
        }
        return false;
    }

    public static List<String> getScriptsForUrl(String url) {
        List<String> strs = new ArrayList<>();
        for(Map.Entry<String, String> u : map.entrySet()) {
            if(url.contains(u.getKey())) strs.add(u.getValue().replace("'app://root'", "'https://myapp'"));
        }
        return strs;
    }
}
