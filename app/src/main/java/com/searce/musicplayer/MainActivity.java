package com.searce.musicplayer;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaMetadataEditor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

interface Communicator {
    public void playback_mode(int id, boolean status);

    public void song_operations(int id);

    public void open_song(int position);

    ArrayList<Song> get_song_list();

    void set_progress(int i);

    void set_volume(float vol);

    void goToPlayer();

    String get_artist();

    String get_album();

    String get_title();

    byte[] get_album_art();

    int get_song_id();

    int get_duration();

    int get_elapsed();

    boolean is_playing();
}

public class MainActivity extends Activity implements Communicator {
    PlayerFragment playerFrag;
    SongListFragment songListFragment;
    MiniPlayerFragment miniPlayerFragment;
    ArrayList<Song> songFiles;
    private MusicService musicSvc;
    private Intent playIntent;
    private boolean musicBound = false;
    private SongCompletedListener songCompletedListener;
    float songVol;

    FragmentManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        if (savedInstanceState == null) {
            playerFrag = new PlayerFragment();
            songListFragment = new SongListFragment();
            miniPlayerFragment = new MiniPlayerFragment();
            songVol = 1;
            songFiles = (ArrayList<Song>) getIntent().getSerializableExtra("songs");
            Collections.sort(songFiles);
        }
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }


    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            //get service
            musicSvc = binder.getService();
            //pass list
            musicSvc.setList(songFiles);
            musicSvc.setSong(0);
            musicBound = true;
            show_list();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    public void toggleFullscreen(boolean fullscreen) {
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
        switch (item.getItemId()) {
            case R.id.action_search:
                break;
            case R.id.action_settings:
                break;
            case R.id.action_rescan:
                Intent rescan = new Intent(MainActivity.this, SplashActivity.class);
                rescan.putExtra("rescan", true);
                startActivity(rescan);
                finish();
                break;
            case R.id.action_about:
                Intent about = new Intent(MainActivity.this, About.class);
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
        switch (id) {
            case R.id.tbRep:
                musicSvc.repeatSongs(status);
                break;
            case R.id.tbShuf:
                musicSvc.shuffleSongs(status);
                break;
        }
    }

    @Override
    public void song_operations(int id) {
        switch (id) {
            case R.id.bPlay:
                togglePlayPause();
                break;
            case R.id.bNext:
                musicSvc.nextSong();
                updateSongInfo();
                break;
            case R.id.bPrev:
                musicSvc.prevSong();
                updateSongInfo();
                break;
            case R.id.bBrowse:
                show_list();
                break;
            case R.id.bVolume:
                AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audio.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
                break;
        }
    }

    private void updateSongInfo() {
        if (playerFrag.isVisible()) {
            playerFrag.updateAlbumArt();
            playerFrag.updateTags();
            playerFrag.setMaxDuration(musicSvc.getDuration());
        } else if (miniPlayerFragment.isVisible()) {
            miniPlayerFragment.updateTags();
            songListFragment.refreshList();
        }
    }

    private void togglePlayPause() {
        musicSvc.togglePlayPause();
    }

    @Override
    public void open_song(int position) {
        musicSvc.setStartIndex(position);
        musicSvc.setSong(position);
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.remove(miniPlayerFragment);
        transaction.remove(songListFragment);

        transaction.add(R.id.container, playerFrag);
        transaction.addToBackStack(null);

        transaction.commit();
        musicSvc.playSong();
    }

    public void show_list() {
        manager = getFragmentManager();
        if (manager.getBackStackEntryCount() > 0)
            manager.popBackStack();
        else {
            FragmentTransaction transaction = manager.beginTransaction();

            transaction.remove(playerFrag);

            transaction.add(R.id.container, songListFragment);
            transaction.add(R.id.container, miniPlayerFragment);
            transaction.commit();
        }
    }

    @Override
    public ArrayList<Song> get_song_list() {
        return songFiles;
    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        unbindService(musicConnection);
        musicSvc = null;
        super.onDestroy();
    }

    @Override
    public void set_progress(int i) {
        musicSvc.seekTo(i);
    }

    @Override
    public void set_volume(float vol) {
        musicSvc.setVolume(vol);
    }

    @Override
    public void goToPlayer() {
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.remove(miniPlayerFragment);
        transaction.remove(songListFragment);

        transaction.add(R.id.container, playerFrag);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public String get_artist() {
        return songFiles.get(musicSvc.playingIndex()).getArtist();
    }

    @Override
    public String get_album() {
        return songFiles.get(musicSvc.playingIndex()).getAlbum();
    }

    @Override
    public String get_title() {
        return songFiles.get(musicSvc.playingIndex()).getTitle();
    }

    @Override
    public byte[] get_album_art() {
        return songFiles.get(musicSvc.playingIndex()).getAlbum_Art(getBaseContext());
    }

    @Override
    public int get_song_id() {
        return musicSvc.playingIndex();
    }

    @Override
    public int get_duration() {
        return musicSvc.getDuration();
    }

    @Override
    public int get_elapsed() {
        return musicSvc.getElapsed();
    }

    @Override
    public boolean is_playing() {
        return musicSvc.isPlaying();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (songCompletedListener == null)
            songCompletedListener = new SongCompletedListener();
        IntentFilter intentFilter = new IntentFilter("Refresh the Song Info");
        registerReceiver(songCompletedListener, intentFilter);
        updateSongInfo();
    }

    @Override
    protected void onPause() {
        if (songCompletedListener != null) unregisterReceiver(songCompletedListener);
        super.onPause();
    }

    private class SongCompletedListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("Refresh the Song Info")) {
                updateSongInfo();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (manager.getBackStackEntryCount() > 0)
            manager.popBackStack();
        else
            moveTaskToBack(true);
    }
}
