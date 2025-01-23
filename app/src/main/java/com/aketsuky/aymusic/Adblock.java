package com.aketsuky.aymusic;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Adblock {
    public static HashMap<String, Boolean> urlBlocked = new HashMap<>();

    public static boolean isAGoodUrl(String url)
    {
        try {
            String[] spl = url.split("/");
            String u = spl[2];
            for (Map.Entry<String, Boolean> str : urlBlocked.entrySet()) {
                if ((str.getValue() && u.contains(str.getKey())) || u.equals(str.getKey())) {
                    return false;
                }
            }
            return true;
        }
        catch (Exception e)
        {
            Log.e("Adblock", "Unable to get url infos : " + url + ". Considering a bad url.");
            Log.e("Adblock", e + "");
            return false;
        }
    }
}
