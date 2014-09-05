package com.searce.musicplayer;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ProgressBar;

/**
 * Created by root on 3/9/14.
 */
public class SplashActivity extends Activity{
    MediaPlayer intro_sound;
    ProgressBar pbLoading;
    boolean showSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getActionBar().hide();
        intro_sound = MediaPlayer.create(getBaseContext(), R.raw.intro_sound);
        pbLoading = (ProgressBar)findViewById(R.id.pbLoading);
        showSplash = false;
        new AsyncSplash().execute();
    }
    public class AsyncSplash extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            if(showSplash)
                intro_sound.start();
            pbLoading.setMax(intro_sound.getDuration());
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Intent startPlayer = new Intent("com.searce.musicplayer.MAIN");
            startActivity(startPlayer);
        }

        @Override
        protected Void doInBackground(Void... params) {
            while(intro_sound.isPlaying()){
                pbLoading.setProgress(intro_sound.getCurrentPosition());
            }
            return null;
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        intro_sound.release();
        finish();
    }
}
