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

    ArrayList<Song> get_song_list();

    MediaPlayer get_song();

    void set_progress(int i);

    void set_volume(float vol);

    void goToPlayer();

    String get_artist();

    String get_album();

    String get_title();

    byte[] get_album_art();

    int get_song_id();
}

public class MainActivity extends Activity implements Communicator, MediaPlayer.OnCompletionListener {
    PlayerFragment playerFrag;
    SongListFragment songListFragment;
    MiniPlayerFragment miniPlayerFragment;
    ArrayList<Song> songFiles;
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
            songFiles = (ArrayList<Song>) getIntent().getSerializableExtra("songs");
            loadFirstSong();
            show_list();
        }
    }

    private void loadFirstSong() {
        try {
            song.setDataSource(getBaseContext(), songFiles.get(songId).getUri());
            song.prepare();
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), songFiles.get(songId).getFile_Name() + " doesn't exist...", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            songId += 1;
            loadFirstSong();
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
            case R.id.action_rescan:
                Intent rescan = new Intent(MainActivity.this, SplashActivity.class);
                rescan.putExtra("rescan", true);
                startActivity(rescan);
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
        if (song.getCurrentPosition() > 3000) {
            song.seekTo(0);
            return;
        }
        songId -= 1;
        if (songId < 0)
            songId += songFiles.size();
        Uri fileUri = songFiles.get(songId).getUri();
        try {
            song.reset();
            song.setDataSource(getBaseContext(), fileUri);
            song.prepare();
            song.start();
            if (playerFrag.isVisible()) {
                playerFrag.updateAlbumArt();
                playerFrag.updateTags();
                playerFrag.setMaxDuration(song.getDuration());
            } else if (miniPlayerFragment.isVisible())
                miniPlayerFragment.updateTags();
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), songFiles.get(songId).getFile_Name() + " doesn't exist...", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            prevSong();
        }
    }

    private void nextSong() {
        songId += 1;
        songId %= songFiles.size();
        Uri fileUri = songFiles.get(songId).getUri();
        try {
            song.reset();
            song.setDataSource(getBaseContext(), fileUri);
            song.prepare();
            song.start();
            if (playerFrag.isVisible()) {
                playerFrag.updateAlbumArt();
                playerFrag.updateTags();
                playerFrag.setMaxDuration(song.getDuration());
            } else if (miniPlayerFragment.isVisible())
                miniPlayerFragment.updateTags();
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), songFiles.get(songId).getFile_Name() + " doesn't exist...", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            nextSong();
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
        Uri fileUri = songFiles.get(songId).getUri();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.remove(miniPlayerFragment);
        transaction.remove(songListFragment);

        transaction.add(R.id.container, playerFrag);
        transaction.commit();
        try {
            song.reset();
            song.setDataSource(getBaseContext(), fileUri);
            song.prepare();
            song.start();
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), songFiles.get(songId).getFile_Name() + " doesn't exist...", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            nextSong();
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
    public ArrayList<Song> get_song_list() {
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
        return songFiles.get(songId).getArtist();
    }

    @Override
    public String get_album() {
        return songFiles.get(songId).getAlbum();
    }

    @Override
    public String get_title() {
        return songFiles.get(songId).getTitle();
    }

    @Override
    public byte[] get_album_art() {
        return songFiles.get(songId).getAlbum_Art(getBaseContext());
    }

    @Override
    public int get_song_id() {
        return songId;
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
