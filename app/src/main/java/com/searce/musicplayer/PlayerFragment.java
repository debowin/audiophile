package com.searce.musicplayer;

import android.app.Fragment;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Created by root on 4/9/14.
 */
public class PlayerFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener {
    Button bPlay, bPrev, bNext;
    ImageView ivAlbumArt;
    ToggleButton tbRep, tbShuf;
    SeekBar seekBar;
    TextView tvElapsed, tvRemaining;
    TextView tvTitle;
    TextView tvAlbum;
    TextView tvArtist;
    Button bList;
    Communicator comm;
    AsyncPlay asyncPlay;
    int new_progress;
    boolean skip_progress_updates;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player,container,false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        comm = (Communicator) getActivity();
        bPlay = (Button) getActivity().findViewById(R.id.bPlay);
        bPrev = (Button) getActivity().findViewById(R.id.bPrev);
        bNext = (Button) getActivity().findViewById(R.id.bNext);
        tbShuf = (ToggleButton) getActivity().findViewById(R.id.tbShuf);
        tbRep = (ToggleButton) getActivity().findViewById(R.id.tbRep);
        seekBar = (SeekBar) getActivity().findViewById(R.id.sbTime);
        tvElapsed = (TextView) getActivity().findViewById(R.id.tvElapsed);
        tvRemaining = (TextView) getActivity().findViewById(R.id.tvRemaining);
        tvTitle = (TextView) getActivity().findViewById(R.id.tvSongTitle_TitleFrag);
        tvAlbum = (TextView) getActivity().findViewById(R.id.tvAlbum_TitleFrag);
        tvArtist = (TextView) getActivity().findViewById(R.id.tvArtist_TitleFrag);
        bList = (Button) getActivity().findViewById(R.id.bBrowse);
        ivAlbumArt = (ImageView) getActivity().findViewById(R.id.ivAlbumArt);
        bList.setOnClickListener(this);
        bPlay.setOnClickListener(this);
        bPrev.setOnClickListener(this);
        bNext.setOnClickListener(this);
        tbRep.setOnCheckedChangeListener(this);
        tbShuf.setOnCheckedChangeListener(this);
        seekBar.setOnSeekBarChangeListener(this);
        tvTitle.setSelected(true);
        tvAlbum.setSelected(true);
        tvArtist.setSelected(true);
        updateAlbumArt();
        updateTags();
        setMaxDuration(comm.get_song().getDuration());
        asyncPlay = new AsyncPlay();
        asyncPlay.execute();
        if (!comm.get_song().isPlaying()) {
            int elapsed = comm.get_song().getCurrentPosition() / 1000;
            int remaining = (comm.get_song().getDuration() - comm.get_song().getCurrentPosition()) / 1000;
            tvElapsed.setText(get_minutes(elapsed) + ":" + get_seconds(elapsed));
            tvRemaining.setText("- " + get_minutes(remaining) + ":" + get_seconds(remaining));
            bPlay.setBackgroundResource(R.drawable.custom_play);
        }
    }

    @Override
    public void onStop() {
        asyncPlay.cancel(true);
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        comm.song_operations(v.getId());
        if(comm.get_song().isPlaying())
            bPlay.setBackgroundResource(R.drawable.custom_pause);
        else
            bPlay.setBackgroundResource(R.drawable.custom_play);
    }
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        comm.playback_mode(compoundButton.getId(), b);
    }

    @Override
    public void onProgressChanged(final SeekBar seekBar, int i, boolean b) {
        if (b) {
            new_progress = i;
            int elapsed = new_progress / 1000;
            int remaining = (seekBar.getMax() - new_progress) / 1000;
            tvElapsed.setText(get_minutes(elapsed) + ":" + get_seconds(elapsed));
            tvRemaining.setText("- " + get_minutes(remaining) + ":" + get_seconds(remaining));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        comm.set_volume(0);
        skip_progress_updates = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        comm.set_progress(new_progress);
        skip_progress_updates = false;
        comm.set_volume(1);
    }

    public class AsyncPlay extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            seekBar.setMax(comm.get_song().getDuration());
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while (true) {
                if (skip_progress_updates) {
                    continue;
                }
                if (comm.get_song().isPlaying()) {
                    if (tvElapsed.getVisibility() == View.INVISIBLE) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvElapsed.setVisibility(View.VISIBLE);
                                tvRemaining.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                    seekBar.setProgress(comm.get_song().getCurrentPosition());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int elapsed = comm.get_song().getCurrentPosition() / 1000;
                            int remaining = (comm.get_song().getDuration() - comm.get_song().getCurrentPosition()) / 1000;
                            tvElapsed.setText(get_minutes(elapsed) + ":" + get_seconds(elapsed));
                            tvRemaining.setText("- " + get_minutes(remaining) + ":" + get_seconds(remaining));
                        }
                    });
                } else {
                    //Blinking effect on pause.
                    if (tvElapsed.getVisibility() == View.VISIBLE) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvElapsed.setVisibility(View.INVISIBLE);
                                tvRemaining.setVisibility(View.INVISIBLE);
                            }
                        });
                    } else if (tvElapsed.getVisibility() == View.INVISIBLE) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvElapsed.setVisibility(View.VISIBLE);
                                tvRemaining.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
                if (isCancelled()) break;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    String get_minutes(int secs) {
        if ((secs / 60) < 10)
            return "0" + String.valueOf(secs / 60);
        return String.valueOf(secs / 60);
    }

    String get_seconds(int secs) {
        if ((secs % 60) < 10)
            return "0" + String.valueOf(secs % 60);
        return String.valueOf(secs % 60);
    }

    public void updateAlbumArt() {
        byte[] bytes = comm.get_album_art();
        if (bytes == null)
            ivAlbumArt.setImageDrawable(getResources().getDrawable(R.drawable.splash));
        else
            ivAlbumArt.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
    }
    public void updateTags() {
        tvTitle.setText(comm.get_title());
        tvAlbum.setText(comm.get_album());
        tvArtist.setText(comm.get_artist());
    }

    public void setMaxDuration(int duration) {
        seekBar.setMax(duration);
    }
}
