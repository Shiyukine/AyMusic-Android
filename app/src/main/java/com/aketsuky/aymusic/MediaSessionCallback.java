package com.aketsuky.aymusic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.session.CommandButton;
import androidx.media3.session.legacy.MediaSessionCompat;
import androidx.media3.session.legacy.PlaybackStateCompat;

@UnstableApi
public class MediaSessionCallback extends MediaSessionCompat.Callback {
    WebView view;
    @SuppressLint("RestrictedApi")
    MediaSessionCompat _mediaSession;
    boolean debouncePause = false;
    boolean isPlaying = false;

    @OptIn(markerClass = UnstableApi.class)
    public MediaSessionCallback(WebView _view, @SuppressLint("RestrictedApi") MediaSessionCompat _session)
    {
        view = _view;
        _mediaSession = _session;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onPlay() {
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !isPlaying[0]) {
                            AudioManager am = (AudioManager)MyService.instance.getSystemService(Context.AUDIO_SERVICE);
                            AudioFocusRequest afr = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                                    .setAudioAttributes(
                                            new AudioAttributes.Builder()
                                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                                    .build()
                                    )
                                    .build();
                            //int aa = am.requestAudioFocus(afr,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
                            int aa = am.requestAudioFocus(afr);
                            if(aa == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                                isPlaying[0] = true;
                                _mediaSession.getController().getTransportControls().play();
                            }
                        }*/
                        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !isPlaying[0]) {
                            AudioManager am = (AudioManager) MyService.instance.getSystemService(Context.AUDIO_SERVICE);
                            AudioFocusRequest afr = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                                    .setAudioAttributes(
                                            new AudioAttributes.Builder()
                                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                                    .build()
                                    )
                                    .setOnAudioFocusChangeListener(new AudioManager.OnAudioFocusChangeListener() {
                                        @Override
                                        public void onAudioFocusChange(int i) {
                                            Log.e("gfdsgdfgdsgdf", "" + i);
                                            if (i == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                                                // Pause playback
                                            } else if (i == AudioManager.AUDIOFOCUS_GAIN) {
                                                // Resume playback
                                                WebAppInterface._mediaSession.getController().getTransportControls().play();
                                            } else if (i == AudioManager.AUDIOFOCUS_LOSS) {
                                                // Stop playback
                                            }
                                        }
                                    })
                                    .build();
                            //int aa = am.requestAudioFocus(afr,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
                            int aa = am.requestAudioFocus(afr);
                            if (aa == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                                //WebAppInterface._mediaSession.getController().getTransportControls().play();
                            }
                        }*/
        view.evaluateJavascript("listeners.player.play()", null);
        isPlaying = false;
        //mediaNotify();
        super.onPlay();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onPause() {
        if(!debouncePause) {
            view.evaluateJavascript("listeners.player.pause()", null);
        }
        debouncePause = false;
        //mediaNotify();
        super.onPause();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onStop() {
        view.evaluateJavascript("listeners.player.pause()", null);
        //mediaNotify();
        super.onStop();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onSkipToNext() {
        view.evaluateJavascript("listeners.player.next()", null);
        //mediaNotify();
        super.onSkipToNext();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onSkipToPrevious() {
        view.evaluateJavascript("listeners.player.previous()", null);
        //mediaNotify();
        super.onSkipToPrevious();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onMediaButtonEvent(final Intent mediaButtonIntent) {
        KeyEvent keyEvent = (KeyEvent) mediaButtonIntent.getExtras().get(Intent.EXTRA_KEY_EVENT);
        assert keyEvent != null;
        Log.d("aa", "GOT MediaButton EVENT " + keyEvent.getKeyCode() + " action: " + keyEvent.getAction());
        if(keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY ||
                    keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PAUSE ||
                    keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (_mediaSession.getController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                    _mediaSession.getController().getTransportControls().pause();
                } else {
                    debouncePause = true;
                    _mediaSession.getController().getTransportControls().play();
                }
            }
        }
        return super.onMediaButtonEvent(mediaButtonIntent);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onSeekTo(long pos) {
        view.evaluateJavascript("listeners.player.seek(" + pos + ")", null);
        //mediaNotify();
        super.onSeekTo(pos);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onCustomAction(String action, @Nullable Bundle extras) {
        String actionName = action.split("_")[0];
        String actionState = action.split("_")[1];
        if(actionName.equals("shuffle"))
        {
            if(actionState.equals("true")) {
                view.evaluateJavascript("listeners.player.setShuffle(false)", null);
                _mediaSession.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE);
            }
            else {
                view.evaluateJavascript("listeners.player.setShuffle(true)", null);
                _mediaSession.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL);
            }
        }
        else if(actionName.equals("repeat"))
        {
            int repeat = Integer.parseInt(actionState);
            if(repeat == 0) {
                view.evaluateJavascript("listeners.player.setRepeat(1)", null);
                _mediaSession.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE);
            }
            else if(repeat == 1) {
                view.evaluateJavascript("listeners.player.setRepeat(2)", null);
                _mediaSession.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL);
            }
            else {
                view.evaluateJavascript("listeners.player.setRepeat(0)", null);
                _mediaSession.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE);
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onSetRepeatMode(int repeatMode) {
        if(repeatMode == PlaybackStateCompat.REPEAT_MODE_ONE)
            view.evaluateJavascript("listeners.player.setRepeat(1)", null);
        else if(repeatMode == PlaybackStateCompat.REPEAT_MODE_ALL)
            view.evaluateJavascript("listeners.player.setRepeat(2)", null);
        else if(repeatMode == PlaybackStateCompat.REPEAT_MODE_NONE)
            view.evaluateJavascript("listeners.player.setRepeat(0)", null);
        _mediaSession.setRepeatMode(repeatMode);
        super.onSetRepeatMode(repeatMode);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onSetShuffleMode(int shuffleMode) {
        if(shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_NONE)
            view.evaluateJavascript("listeners.player.setShuffle(false)", null);
        else
            view.evaluateJavascript("listeners.player.setShuffle(true)", null);
        _mediaSession.setShuffleMode(shuffleMode);
        super.onSetShuffleMode(shuffleMode);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onPlayFromMediaId(@Nullable String mediaId, @Nullable Bundle extras) {
        //mediaId must contains 3 parts: context;objId;SongId
        if(mediaId.split(";").length == 3)
        {
            String context = mediaId.split(";")[0];
            String objId = mediaId.split(";")[1];
            String songId = mediaId.split(";")[2];
            if(context.equals("queue"))
            {
                view.evaluateJavascript("listeners.queue.moveTo('" + songId + "')", null);
            }
        }
    }
}
