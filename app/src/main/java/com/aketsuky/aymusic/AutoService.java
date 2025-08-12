package com.aketsuky.aymusic;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaDescription;
import android.media.browse.MediaBrowser;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ValueCallback;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.session.MediaConstants;
import androidx.media3.session.legacy.MediaBrowserCompat;
import androidx.media3.session.legacy.MediaBrowserServiceCompat;
import androidx.media3.session.legacy.MediaDescriptionCompat;
import androidx.media3.session.legacy.MediaSessionCompat;
import androidx.media3.session.legacy.PlaybackStateCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("RestrictedApi")
@UnstableApi
public class AutoService extends MediaBrowserServiceCompat {

    final String ROOT_ID = "/";
    final String ROOT_ID_VOID = "/void";
    final String ROOT_ID_QUEUE = "/queue";

    @Override
    public void onCreate() {
        super.onCreate();
        setSessionToken(WebAppInterface._mediaSession.getSessionToken());
    }

    @SuppressLint("RestrictedApi")
    @Nullable
    @Override
    public BrowserRoot onGetRoot(@Nullable String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new MediaBrowserServiceCompat.BrowserRoot(ROOT_ID, null);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onLoadChildren(@Nullable String parentId, Result<List<MediaBrowserCompat.MediaItem>> result) {
        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();
        if (ROOT_ID_VOID.equals(parentId)) {
            result.sendResult(new ArrayList<MediaBrowserCompat.MediaItem>());
        }
        else if (ROOT_ID.equals(parentId)) {
            MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                    .setMediaId(ROOT_ID_QUEUE)
                    .setTitle("Queue")
                    .setSubtitle("View your current queue")
                    .build();
            mediaItems.add(new MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));
            result.sendResult(mediaItems);
        }
        else if (ROOT_ID_QUEUE.equals(parentId)) {
            result.detach();
            MainActivity.actualWb.evaluateJavascript("listeners.queue.view()", new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {
                    try {
                        String jsonString = URLDecoder.decode( value, "UTF-8" ).substring(1, value.length() - 1); // Remove outer quotes
                        jsonString = jsonString.replace("\\\"", "\""); // Unescape quotes
                        jsonString = jsonString.replace("\\\\", "\\"); // Unescape backslashes
                        JSONArray songs = new JSONArray(jsonString);
                        for(int i = 0; i < songs.length(); i++) {
                            JSONObject song = songs.getJSONObject(i);
                            try {
                                MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                                        .setTitle(song.getString("title"))
                                        .setSubtitle(song.getString("artist") + " • " + song.getString("album"))
                                        .setMediaId("queue;" + song.getString("objId") + ";" + song.getString("id"))
                                        .setIconUri(Uri.parse(song.getString("icon")))
                                        .build();
                                mediaItems.add(new MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
                            }
                            catch (Exception e)
                            {
                                try
                                {
                                    String newUrl = "/resources/icon.ico".substring(1);
                                    AssetManager am = WebAppInterface.mainActivity.getAssets();
                                    InputStream is = am.open(newUrl);
                                    Bitmap myBitmap = BitmapFactory.decodeStream(is);
                                    MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                                            .setTitle(song.getString("title"))
                                            .setSubtitle(song.getString("artist") + " • " + song.getString("album"))
                                            .setMediaId("queue;" + song.getString("objId") + ";" + song.getString("id"))
                                            .setIconBitmap(myBitmap)
                                            .build();
                                    mediaItems.add(new MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
                                }
                                catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                        result.sendResult(mediaItems);
                    } catch (Exception e) {
                        e.printStackTrace();
                        MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                                .setMediaId(ROOT_ID_VOID)
                                .setTitle("Error")
                                .setSubtitle("Cannot load your current queue")
                                .build();
                        mediaItems.add(new MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));
                        result.sendResult(mediaItems);
                    }
                }
            });
        }
    }

    private MediaBrowser.MediaItem createBrowsableMediaItem(
            String mediaId,
            String folderName,
            Uri iconUri) {
        MediaDescription.Builder mediaDescriptionBuilder = new MediaDescription.Builder();
        mediaDescriptionBuilder.setMediaId(mediaId);
        mediaDescriptionBuilder.setTitle(folderName);
        mediaDescriptionBuilder.setIconUri(iconUri);
        return new MediaBrowser.MediaItem(
                mediaDescriptionBuilder.build(), MediaBrowser.MediaItem.FLAG_BROWSABLE);
    }

    private MediaBrowser.MediaItem createMediaItem(String mediaId, String folderName, Uri iconUri) {
        MediaDescription.Builder mediaDescriptionBuilder = new MediaDescription.Builder();
        mediaDescriptionBuilder.setMediaId(mediaId);
        mediaDescriptionBuilder.setTitle(folderName);
        mediaDescriptionBuilder.setIconUri(iconUri);
        Bundle extras = new Bundle();
        extras.putString(
                MediaConstants.EXTRAS_KEY_CONTENT_STYLE_GROUP_TITLE,
                "Songs");
        mediaDescriptionBuilder.setExtras(extras);
        return new MediaBrowser.MediaItem(
                mediaDescriptionBuilder.build(), MediaBrowser.MediaItem.FLAG_BROWSABLE);
    }
}