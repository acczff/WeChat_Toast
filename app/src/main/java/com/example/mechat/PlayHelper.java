package com.example.mechat;

import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;

/**
 * Created by Bour on 2018/7/15.
 */

public class PlayHelper {
    private static PlayHelper playHelper = null;
    AudioTrack track;
    boolean isRun = false;

    private PlayHelper() {
        this.track = new AudioTrack(AudioManager.STREAM_MUSIC, RecordHelper.HZ, RecordHelper.CHANNEL, RecordHelper.AUDIO_FORMAT, RecordHelper.getMinBuffer(), AudioTrack.MODE_STREAM);
    }

    public static PlayHelper load() {
        if (playHelper == null)
            playHelper = new PlayHelper();
        return playHelper;
    }

    public void ready() {
        track.play();
    }

    public void write(byte[] pcm,int offset,int len) {
        track.write(pcm, offset, len);
    }

    public void close() {
        this.track.release();
        this.track = null;
        playHelper = null;
    }
}
