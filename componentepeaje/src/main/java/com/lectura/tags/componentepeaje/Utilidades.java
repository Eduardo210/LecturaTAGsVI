package com.lectura.tags.componentepeaje;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

public class Utilidades {

    private MediaPlayer mSucessMediaPlayer = null;
    private MediaPlayer mErrorMediaPlayer = null;
    private Context context;

    public Utilidades(Context context) {

        this.context = context;

        mSucessMediaPlayer = MediaPlayer.create(context, R.raw.success);
        mSucessMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mErrorMediaPlayer = MediaPlayer.create(context, R.raw.fail);
        mErrorMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public void playSuccess() {
        if(null != mSucessMediaPlayer) {
            mSucessMediaPlayer.seekTo(0);
            mSucessMediaPlayer.start();
        }
    }

    public void playError() {
        if(null != mErrorMediaPlayer) {
            mErrorMediaPlayer.seekTo(0);
            mErrorMediaPlayer.start();
        }
    }



}
