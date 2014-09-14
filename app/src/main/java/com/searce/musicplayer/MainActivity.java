package com.searce.musicplayer;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.media.MediaMetadataEditor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

interface Communicator{
    public void playback_mode(int id, boolean status);

    public void song_operations(int id);

    public void open_song(int position);

    ArrayList<String> get_song_list();

    MediaPlayer get_song();

    void set_progress(int i);

    void set_volume(float vol);

    void goToPlayer();

    String get_artist();

    String get_album();

    String get_title();

    byte[] get_album_art();

    int get_song_id();

    ArrayList<String> get_song_durations();

    ArrayList<String> get_song_artists();

    ArrayList<String> get_song_titles();
}

public class MainActivity extends Activity implements Communicator, MediaPlayer.OnCompletionListener {
    PlayerFragment playerFrag;
    SongListFragment songListFragment;
    MiniPlayerFragment miniPlayerFragment;
    ArrayList<String> songFiles;
    ArrayList<String> songTitles;
    ArrayList<String> songArtists;
    ArrayList<String> songDurations;
    MediaPlayer song;
    MediaMetadataRetriever meta_getter;
    int songId;
    float songVol;

    FragmentManager manager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        if (savedInstanceState == null) {
            song = new MediaPlayer();
            playerFrag = new PlayerFragment();
            songListFragment = new SongListFragment();
            miniPlayerFragment = new MiniPlayerFragment();
            meta_getter = new MediaMetadataRetriever();
            songVol = 0.5f;
            song.setOnCompletionListener(this);
            songId = 0;
            songFiles = getIntent().getStringArrayListExtra("songs_paths");
            songTitles = getIntent().getStringArrayListExtra("songs_titles");
            songArtists = getIntent().getStringArrayListExtra("songs_artists");
            songDurations = getIntent().getStringArrayListExtra("songs_durations");
            try {
                song.setDataSource(getBaseContext(), Uri.parse(songFiles.get(songId)));
                song.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            show_list();
        }
    }

    public void toggleFullscreen(boolean fullscreen)
    {
        if (fullscreen)
            getActionBar().hide();
        else
            getActionBar().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.action_search:
                break;
            case R.id.action_settings:
                break;
            case R.id.action_about:
                Intent about = new Intent(MainActivity.this,About.class);
                startActivity(about);
                break;
            case R.id.action_exit:
                finish();
                break;
        }
        return false;
    }

    @Override
    public void playback_mode(int id, boolean status) {
        switch(id){
            case R.id.tbRep:
                if (status)
                    Toast.makeText(getBaseContext(), "Repeat Enabled", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getBaseContext(), "Repeat Disabled", Toast.LENGTH_SHORT).show();
                break;
            case R.id.tbShuf:
                if (status)
                    Toast.makeText(getBaseContext(), "Shuffle Enabled", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getBaseContext(), "Shuffle Disabled", Toast.LENGTH_SHORT).show();
                break;
        }
    }
    @Override
    public void song_operations(int id) {
        switch(id){
            case R.id.bPlay:
                togglePlayPause();
                break;
            case R.id.bNext:
                nextSong();
                break;
            case R.id.bPrev:
                prevSong();
                break;
            case R.id.bBrowse:
                show_list();
        }
    }

    private void prevSong() {
        if (songId == 0)
            return;
        //TODO: If on repeat, start playing again
        if (song.getCurrentPosition() > 3000) {
            song.seekTo(0);
            return;
        }
        songId -= 1;
        playerFrag.updateTags();
        miniPlayerFragment.updateTags();
        playerFrag.updateAlbumArt();
        String filename = songFiles.get(songId);
        try {
            song.reset();
            song.setDataSource(getBaseContext(), Uri.parse(filename));
            song.prepare();
            song.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void nextSong() {
        if (songId + 1 == songFiles.size())
            return;
        //TODO: If on repeat, start playing again
        songId += 1;
        if (playerFrag.isVisible()) {
            playerFrag.updateAlbumArt();
            playerFrag.updateTags();
        } else if (miniPlayerFragment.isVisible())
            miniPlayerFragment.updateTags();
        String filename = songFiles.get(songId);
        try {
            song.reset();
            song.setDataSource(getBaseContext(), Uri.parse(filename));
            song.prepare();
            song.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void togglePlayPause() {
        if (song.isPlaying()) {
            song.pause();
        } else {
            song.start();
        }
    }

    @Override
    public void open_song(int position) {
        songId = position;
        String filename = songFiles.get(songId);
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.remove(miniPlayerFragment);
        transaction.remove(songListFragment);

        transaction.add(R.id.container, playerFrag);
        transaction.commit();
        try {
            song.reset();
            song.setDataSource(getBaseContext(), Uri.parse(filename));
            song.prepare();
            song.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void show_list() {
        manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.remove(playerFrag);

        transaction.add(R.id.container, songListFragment);
        transaction.add(R.id.container, miniPlayerFragment);
        transaction.commit();
    }

    @Override
    public ArrayList<String> get_song_list() {
        return songFiles;
    }


    @Override
    public MediaPlayer get_song() {
        return song;
    }

    @Override
    public void set_progress(int i) {
        song.seekTo(i);
    }

    @Override
    public void set_volume(float vol) {
        song.setVolume(vol, vol);
    }

    @Override
    public void goToPlayer() {
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.remove(miniPlayerFragment);
        transaction.remove(songListFragment);

        transaction.add(R.id.container, playerFrag);
        transaction.commit();
    }

    @Override
    public String get_artist() {
        meta_getter.setDataSource(songFiles.get(songId));
        String artist = meta_getter.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        if (artist == null || artist.contentEquals("")) {
            return "Unknown Artist";
        } else
            return artist;
    }

    @Override
    public String get_album() {
        meta_getter.setDataSource(songFiles.get(songId));
        String album = meta_getter.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        if (album == null || album.contentEquals("")) {
            return "Untitled Album";
        } else
            return album;
    }

    @Override
    public String get_title() {
        meta_getter.setDataSource(songFiles.get(songId));
        String title = meta_getter.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        if (title == null || title.contentEquals("")) {
            String fake_title = new File(songFiles.get(songId)).getName();
            return fake_title.substring(0,fake_title.length()-4);
        } else
            return title;
    }

    @Override
    public byte[] get_album_art() {
        meta_getter.setDataSource(songFiles.get(songId));
        return meta_getter.getEmbeddedPicture();
    }

    @Override
    public int get_song_id() {
        return songId;
    }

    @Override
    public ArrayList<String> get_song_durations() {
        return songDurations;
    }

    @Override
    public ArrayList<String> get_song_artists() {
        return songArtists;
    }

    @Override
    public ArrayList<String> get_song_titles() {
        return songTitles;
    }

    @Override
    protected void onStop() {
        song.release();
        finish();
        super.onStop();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        //Auto switch to next song on completion
        nextSong();
    }

}
