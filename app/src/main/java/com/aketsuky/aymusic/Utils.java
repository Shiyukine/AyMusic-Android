package com.aketsuky.aymusic;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.webkit.MimeTypeMap;

import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Objects;

public class Utils {
    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

    public static String getMimetypeFromUrl(String url)
    {
        if(Build.VERSION.SDK_INT >= 29)
        {
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));
        }
        else {
            String ret = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));
            if(ret == null)
            {
                String ext = MimeTypeMap.getFileExtensionFromUrl(url);
                if(Objects.equals(ext, "js")) ret = "text/javascript";
                if(Objects.equals(ext, "json")) ret = "application/json";
            }
            return ret;
        }
    }
}
