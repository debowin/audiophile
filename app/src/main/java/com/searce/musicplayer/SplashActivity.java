package com.searce.musicplayer;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Created by root on 3/9/14.
 */
public class SplashActivity extends Activity{
    ProgressBar pbLoading;
    TextView tvFound;
    ArrayList<Song> songFiles;
    int numFilesFound;
    File listfile;
    boolean exited;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getActionBar().hide();
        pbLoading = (ProgressBar)findViewById(R.id.pbLoading);
        tvFound = (TextView) findViewById(R.id.tvFound);
        numFilesFound = 0;
        songFiles = new ArrayList<Song>();
        tvFound.setText("Found nothing...");
        exited = false;
        new AsyncContentResolve().execute();
    }

    private void read_data() {
        try {
            listfile = new File(getBaseContext().getFilesDir(), "listfile");
            FileInputStream fis = new FileInputStream(listfile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            int lines = ois.readInt();
            for (int i = 0; i < lines; i++) {
                Song song = (Song) ois.readObject();
                songFiles.add(song);
            }
            ois.close();
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void write_data() {
        try {
            listfile = new File(getBaseContext().getFilesDir(), "listfile");
            FileOutputStream fos = new FileOutputStream(listfile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeInt(songFiles.size());
            for (Song song : songFiles) {
                oos.writeObject(song);
            }
            oos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void fetch_list() {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pbLoading.setMax(musicCursor.getCount());
            }
        });

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);
            int albumColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            int durationColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DURATION);
            int displayNameColumn = musicCursor.getColumnIndex
                    (String.valueOf(MediaStore.Audio.Media.DISPLAY_NAME));
            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisFileName = musicCursor.getString(displayNameColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                String thisDuration = musicCursor.getString(durationColumn);
                // Just for the heck of it, showing off the loading bar.
//                try {
//                    Thread.sleep(10);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                // Filter out songs less than 5 seconds in length.
                if (thisDuration != null && Integer.valueOf(thisDuration) > 5000) {
                    songFiles.add(new Song(thisId, thisTitle, thisFileName, thisArtist, thisAlbum, thisDuration));
                    numFilesFound++;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvFound.setText("Found " + numFilesFound + " files...");
                        pbLoading.setProgress(numFilesFound);
                    }
                });
                if (exited)
                    break;
            }
            while (musicCursor.moveToNext());
        }
    }

    @Override
    protected void onStop() {
        exited = true;
        finish();
        super.onStop();
    }

    private class AsyncContentResolve extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
//            if (!rescan) {
//                // Try to fetch results from Internal Storage.
//                read_data();
//            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (exited) {
                return;
            }
            // Write results to Internal Storage for future usage.
            //write_data();
            Intent main = new Intent(getBaseContext(), MainActivity.class);
            main.putExtra("songs", songFiles);
            startActivity(main);
            finish();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            fetch_list(); // Call to the content resolver.
            return null;
        }
    }
}
