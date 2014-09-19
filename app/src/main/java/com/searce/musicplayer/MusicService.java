package com.searce.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Observer;

/**
 * Created by debowin on 15/9/14.
 */
public class MusicService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private MediaPlayer player;
    private ArrayList<Song> playlist;
    private int songPosn;
    private final IBinder musicBind = new MusicBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        songPosn = 0;
        player = new MediaPlayer();
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        initMusicPlayer();
    }

    public void initMusicPlayer() {
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    public void setList(ArrayList<Song> list) {
        playlist = list;
    }

    public void togglePlayPause() {
        if (player.isPlaying()) {
            player.pause();
        } else {
            player.start();
        }
    }

    public void seekTo(int posn) {
        player.seekTo(posn);
    }

    public void setVolume(float vol) {
        player.setVolume(vol, vol);
    }

    public int playingIndex() {
        return songPosn;
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public int getElapsed() {
        return player.getCurrentPosition();
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    public void playSong() {
        player.start();
    }

    public void setSong(int newPosn) {
        songPosn = newPosn;
        player.reset();
        Song currsong = playlist.get(songPosn);
        Uri fileUri = currsong.getUri();
        try {
            player.setDataSource(getBaseContext(), fileUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void prevSong() {
        if (player.getCurrentPosition() > 3000) {
            player.stop();
            player.start();
            return;
        }
        songPosn -= 1;
        if (songPosn < 0)
            // TODO: Only for repeat
            songPosn += playlist.size();
        setSong(songPosn);
        playSong();
    }

    public void nextSong() {
        songPosn += 1;
        songPosn %= playlist.size();
        setSong(songPosn);
        // TODO: Only for repeat
        playSong();
    }

    public int getDuration() {
        return player.getDuration();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        nextSong();
        sendBroadcast(new Intent("Refresh the Song Info"));
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
        return false;
    }
}
