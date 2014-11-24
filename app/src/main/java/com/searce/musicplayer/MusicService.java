package com.searce.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by debowin on 15/9/14.
 */
public class MusicService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private MediaPlayer player;
    private ArrayList<Song> playlist;
    private int songPosn;
    private boolean shuffle, repeat;
    private final IBinder musicBind = new MusicBinder();
    private ArrayList<Integer> shuffle_list;
    private int startIndex;
    private float volume;

    @Override
    public void onCreate() {
        super.onCreate();
        volume = .50f;
        songPosn = 0;
        startIndex = 0;
        shuffle = false;
        repeat = false;
        shuffle_list = new ArrayList<Integer>();
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
        for (int i = 0; i < playlist.size(); i++)
            shuffle_list.add(i);
    }

    public void shuffleSongs(boolean status) {
        if (!shuffle && status) {
            Toast.makeText(getBaseContext(), "Shuffle Enabled", Toast.LENGTH_SHORT).show();
            long seed = System.nanoTime();
            Collections.shuffle(shuffle_list, new Random(seed));
            songPosn = shuffle_list.lastIndexOf(songPosn);
            setStartIndex(songPosn);
        } else if (shuffle && !status) {
            Toast.makeText(getBaseContext(), "Shuffle Disabled", Toast.LENGTH_SHORT).show();
            songPosn = shuffle_list.get(songPosn);
            for (int i = 0; i < playlist.size(); i++)
                shuffle_list.set(i, i);
        }
        shuffle = status;
    }

    public void repeatSongs(boolean status, int song_id) {
        if (!repeat && status)
            Toast.makeText(getBaseContext(), "Repeat Enabled", Toast.LENGTH_SHORT).show();
        else if (repeat && !status) {
            Toast.makeText(getBaseContext(), "Repeat Disabled", Toast.LENGTH_SHORT).show();
            setStartIndex(song_id); // Whenever repeat is disabled, the current song should become the start index for repeat.
        }
        repeat = status;
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
        volume = vol;
        player.setVolume(vol, vol);
    }

    public float getVolume() {
        return volume;
    }

    public int playingIndex() {
        return shuffle_list.get(songPosn);
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public int getElapsed() {
        return player.getCurrentPosition();
    }

    public void setStartIndex(int startIndex) {
        // To set a starting point for repeat.
        // Once playback reaches this point and repeat is disabled, stop playback.
        this.startIndex = startIndex;
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
        songPosn = shuffle_list.lastIndexOf(newPosn);
        player.reset();
        Song currsong = playlist.get(newPosn);
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
            player.seekTo(0);
            return;
        }
        songPosn -= 1;
        if (songPosn < 0)
            songPosn += playlist.size();
        setSong(shuffle_list.get(songPosn));
        playSong();
    }

    public void nextSong() {
        if (!repeat && (songPosn + 1) % playlist.size() == startIndex) {
            Toast.makeText(getBaseContext(), "End of Playlist", Toast.LENGTH_SHORT).show();
            setSong(shuffle_list.get(songPosn));
            seekTo(0);
        } else {
            songPosn += 1;
            songPosn %= playlist.size();
            setSong(shuffle_list.get(songPosn));
            playSong();
        }
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
