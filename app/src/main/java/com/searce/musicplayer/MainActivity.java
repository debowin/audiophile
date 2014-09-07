package com.searce.musicplayer;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

interface Communicator{
    public void playback_mode(int id, boolean status);

    public void song_operations(int id);

    public void open_song(int position);

    public void show_list();

    ArrayList<File> get_song_list();

    public void set_song_list(ArrayList<File> songs);

    MediaPlayer get_song();

    void set_progress(int i);

    void set_volume(float vol);
}

public class MainActivity extends Activity implements Communicator{
    PlayerFragment playerFrag;
    AlbumArtFragment artFrag;
    TitleFrag titleFrag;
    SongListFragment songListFragment;
    CategoryFragment categoryFragment;
    MiniPlayerFragment miniPlayerFragment;
    ArrayList<File> songFiles;
    MediaPlayer song;
    int songId;
    float songVol;

    FragmentManager manager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        if (savedInstanceState == null) {
            songFiles = new ArrayList<File>();
            song = new MediaPlayer();
            playerFrag = new PlayerFragment();
            artFrag = new AlbumArtFragment();
            titleFrag = new TitleFrag();
            songListFragment = new SongListFragment();
            categoryFragment = new CategoryFragment();
            miniPlayerFragment = new MiniPlayerFragment();
            songVol = 0.5f;
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
                if(status == true)
                    titleFrag.changeTitle("Repeat Enabled");
                else
                    titleFrag.changeTitle("Repeat Disabled");
                break;
            case R.id.tbShuf:
                if(status == true)
                    titleFrag.changeTitle("Shuffle Enabled");
                else
                    titleFrag.changeTitle("Shuffle Disabled");
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
        String filename = songFiles.get(songId).toString();
        try {
            song.reset();
            song.setDataSource(getBaseContext(), Uri.parse(filename));
            song.prepare();
            song.start();
            playerFrag.playPause("play");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void nextSong() {
        if (songId + 1 == songFiles.size())
            return;
        //TODO: If on repeat, start playing again
        songId += 1;
        String filename = songFiles.get(songId).toString();
        try {
            song.reset();
            song.setDataSource(getBaseContext(), Uri.parse(filename));
            song.prepare();
            song.start();
            playerFrag.playPause("play");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void togglePlayPause() {
        if (song.isPlaying()) {
            playerFrag.playPause("pause");
            song.pause();
        } else {
            playerFrag.playPause("play");
            song.start();
        }
    }

    @Override
    public void open_song(int position) {
        songId = position;
        String filename = songFiles.get(songId).toString();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.remove(categoryFragment);
        transaction.remove(songListFragment);
        transaction.remove(miniPlayerFragment);

        transaction.add(R.id.container, playerFrag);
        transaction.add(R.id.container, artFrag);
        transaction.add(R.id.container, titleFrag);
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

    @Override
    public void show_list() {
        manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.remove(playerFrag);
        transaction.remove(artFrag);
        transaction.remove(titleFrag);
        transaction.add(R.id.container, categoryFragment);
        transaction.add(R.id.container, songListFragment);
        transaction.add(R.id.container, miniPlayerFragment);

        transaction.commit();
    }

    @Override
    public ArrayList<File> get_song_list() {
        return songFiles;
    }

    @Override
    public void set_song_list(ArrayList<File> songs) {
        songFiles = songs;
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
        if (vol < 0)
            song.setVolume(songVol, songVol);
        else
            song.setVolume(vol, vol);
    }

    @Override
    protected void onStop() {
        song.release();
        finish();
        super.onStop();
    }
}
