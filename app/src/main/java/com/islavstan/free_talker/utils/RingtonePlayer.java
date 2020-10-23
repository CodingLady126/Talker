package com.islavstan.free_talker.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import com.islavstan.free_talker.R;


/**
 * QuickBlox team
 */
public class RingtonePlayer {

    private static final String TAG = RingtonePlayer.class.getSimpleName();
    private MediaPlayer mediaPlayer;
    private Context context;
    private AudioManager m_amAudioManager;

    public RingtonePlayer(Context context, int resource, int type) {
        this.context = context;
        mediaPlayer = android.media.MediaPlayer.create(context, resource);
        m_amAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (type == 0)
            m_amAudioManager.setMode(AudioManager.MODE_IN_CALL);
        else
            m_amAudioManager.setMode(AudioManager.MODE_RINGTONE);
    }

    public RingtonePlayer(Context context) {
        this.context = context;
        mediaPlayer = android.media.MediaPlayer.create(context, R.raw.ringtone);
        m_amAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        m_amAudioManager.setMode(AudioManager.MODE_RINGTONE);

    }

    private Uri getNotification() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

        if (notification == null) {
            // notification is null, using backup
            notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            // I can't see this ever being null (as always have a default notification)
            // but just incase
            if (notification == null) {
                // notification backup is null, using 2nd backup
                notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            }
        }
        return notification;
    }

    public void play(boolean looping) {
        Log.i(TAG, "play");
        if (mediaPlayer == null) {
            Log.i(TAG, "mediaPlayer isn't created ");
            return;
        }
        mediaPlayer.setLooping(looping);
        mediaPlayer.start();
    }

    public synchronized void stop() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
