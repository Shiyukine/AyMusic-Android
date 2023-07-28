package com.aketsuky.aymusic;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class downloadFile extends AsyncTask<String, String, File> {
    MainActivity mainActivity;
    String savePath;

    public downloadFile(MainActivity mainActivity, String savePath) {
        this.mainActivity = mainActivity;
        this.savePath = savePath;
    }

    @Override
    protected File doInBackground(String... urls) {
        try {
            URLConnection connection = (new URL(urls[0])).openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();
            InputStream input = connection.getInputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            int total = 0;
            File dir = new File(mainActivity.getExternalFilesDir(null) + savePath.substring(0, savePath.lastIndexOf("/")));
            if (!dir .exists()) {
                dir.mkdirs();
            }
            File tempFile = new File(mainActivity.getExternalFilesDir(null) + savePath);
            OutputStream output = new FileOutputStream(tempFile);
            int lenghtOfFile = connection.getContentLength();
            while ((bytesRead = input.read(buffer)) != -1)
            {
                total += bytesRead;
                publishProgress("" + total, "" + lenghtOfFile);
                output.write(buffer, 0, bytesRead);
            }
            output.flush();
            output.close();
            input.close();
            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
