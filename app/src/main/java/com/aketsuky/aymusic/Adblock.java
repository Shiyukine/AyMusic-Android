package com.aketsuky.aymusic;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Adblock {
    public static List<String> urlBlocked = new ArrayList<>();

    public static boolean isAGoodUrl(String url)
    {
        try {
            String[] spl = url.split("/");
            String u = spl[2];
            for (String str : urlBlocked) {
                if (u.contains(str)) {
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
