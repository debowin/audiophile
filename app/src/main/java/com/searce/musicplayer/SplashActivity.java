package com.searce.musicplayer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by root on 3/9/14.
 */
public class SplashActivity extends Activity{
    ProgressBar pbLoading;
    TextView tvFound;
    boolean showSplash;
    ArrayList<String> songFiles;
    ArrayList<String> songTitles;
    ArrayList<String> songArtists;
    ArrayList<String> songDurations;
    int numFilesFound;
    File listfile;
    boolean exited;
    boolean noneed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean rescan = getIntent().getBooleanExtra("rescan", false);
        setContentView(R.layout.activity_splash);
        getActionBar().hide();
        pbLoading = (ProgressBar)findViewById(R.id.pbLoading);
        tvFound = (TextView) findViewById(R.id.tvFound);
        showSplash = true;
        numFilesFound = 0;
        exited = false;
        songFiles = new ArrayList<String>();
        songTitles = new ArrayList<String>();
        songArtists = new ArrayList<String>();
        songDurations = new ArrayList<String>();
        tvFound.setText("Found no files so far...");
        noneed = false;
        if (rescan) {
            new AsyncFileScan().execute();
            return;
        }
        //Try to fetch results from Internal Storage.
        try {
            listfile = new File(getBaseContext().getFilesDir(), "listfile");
            FileInputStream fis = new FileInputStream(listfile);
            DataInputStream dis = new DataInputStream(fis);
            int lines = dis.readInt();
            for (int i = 0; i < lines; i++) {
                String line = dis.readUTF();
                songFiles.add(line);
            }
            for (int i = 0; i < lines; i++) {
                String line = dis.readUTF();
                songTitles.add(line);
            }
            for (int i = 0; i < lines; i++) {
                String line = dis.readUTF();
                songArtists.add(line);
            }
            for (int i = 0; i < lines; i++) {
                String line = dis.readUTF();
                songDurations.add(line);
            }
            dis.close();
            fis.close();
            noneed = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        new AsyncFileScan().execute();
    }

    public class AsyncFileScan extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            if (noneed)
                return null;
            filewalker(new File("/storage/sdcard1"));
//            filewalker(Environment.getExternalStorageDirectory().getParent());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (exited)
                return;
            //Store results into Internal Storage.
            try {
                listfile = new File(getBaseContext().getFilesDir(), "listfile");
                FileOutputStream fos = new FileOutputStream(listfile);
                DataOutputStream dos = new DataOutputStream(fos);
                dos.writeInt(songFiles.size());
                for (String songFile : songFiles) {
                    dos.writeUTF(songFile);
                }
                for (String songTitle : songTitles) {
                    dos.writeUTF(songTitle);
                }
                for (String songArtist : songArtists) {
                    dos.writeUTF(songArtist);
                }
                for (String songDuration : songDurations) {
                    dos.writeUTF(songDuration);
                }
                dos.flush();
                dos.close();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Intent startPlayer = new Intent(getBaseContext(), MainActivity.class);
            startPlayer.putStringArrayListExtra("songs_paths", songFiles);
            startPlayer.putStringArrayListExtra("songs_titles", songTitles);
            startPlayer.putStringArrayListExtra("songs_artists", songArtists);
            startPlayer.putStringArrayListExtra("songs_durations", songDurations);
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
                        //Add metas to list
                        MediaMetadataRetriever mdr = new MediaMetadataRetriever();
                        mdr.setDataSource(aListFile.getPath());
                        String title = mdr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                        if (title == null || title.contentEquals("")) {
                            String fake_title = new File(aListFile.getPath()).getName();
                            title = fake_title.substring(0, fake_title.length() - 4);
                        }
                        String artist = mdr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                        if (artist == null || artist.contentEquals("")) {
                            artist = "Unknown Artist";
                        }

                        int duration = MediaPlayer.create(getBaseContext(), Uri.parse(aListFile.getPath())).getDuration();
                        String minutes = String.valueOf(duration / 60000);
                        String seconds = String.valueOf((duration / 1000) % 60);
                        if (minutes.length() == 1) {
                            minutes = "0" + String.valueOf(minutes);
                        }
                        if (seconds.length() == 1) {
                            seconds = "0" + String.valueOf(seconds);
                        }

                        songTitles.add(title);
                        songArtists.add(artist);
                        songDurations.add(minutes + ":" + seconds);
                        numFilesFound++;
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvFound.setText("Found " + numFilesFound + " files so far...");
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
