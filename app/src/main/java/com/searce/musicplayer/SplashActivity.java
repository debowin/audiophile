package com.searce.musicplayer;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by root on 3/9/14.
 */
public class SplashActivity extends Activity{
    MediaPlayer intro_sound;
    ProgressBar pbLoading;
    TextView tvFound;
    boolean showSplash;
    ArrayList<String> songFiles;
    int numFiles;
    boolean exited;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getActionBar().hide();
        pbLoading = (ProgressBar)findViewById(R.id.pbLoading);
        tvFound = (TextView) findViewById(R.id.tvFound);
        showSplash = true;
        numFiles = 0;
        exited = false;
        songFiles = new ArrayList<String>();
        new AsyncFileScan().execute();
    }

    public class AsyncFileScan extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            filewalker(new File("/storage/sdcard1"));
//            filewalker(Environment.getExternalStorageDirectory());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (exited)
                return;
            Intent startPlayer = new Intent(getBaseContext(), MainActivity.class);
            startPlayer.putStringArrayListExtra("songs", songFiles);
            startActivity(startPlayer);
        }
    }

    public void filewalker(File dir) {
        String mp3Pattern = ".mp3";
        if (exited)
            return;
        File[] listFile = dir.listFiles();

        if (listFile != null) {
            for (File aListFile : listFile) {

                if (aListFile.isDirectory()) {
                    filewalker(aListFile);
                } else {
                    if (aListFile.getName().endsWith(mp3Pattern)) {
                        //Add files to list
                        songFiles.add(aListFile.getPath());
                        numFiles++;
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvFound.setText("Found " + numFiles + " files so far...");
                            }
                        });
                    }
                }
            }
        }
    }

    @Override
    protected void onStop() {
        exited = true;
        finish();
        super.onStop();
    }
}
