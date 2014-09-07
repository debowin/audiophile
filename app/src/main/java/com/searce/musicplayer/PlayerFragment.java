package com.searce.musicplayer;

import android.app.Fragment;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Created by root on 4/9/14.
 */
public class PlayerFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener {
    Button bPlay, bPrev, bNext;
    ToggleButton tbRep, tbShuf;
    SeekBar seekBar;
    TextView tvElapsed, tvRemaining;
    Communicator comm;
    AsyncPlay asyncPlay;

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
        bPlay.setOnClickListener(this);
        bPrev.setOnClickListener(this);
        bNext.setOnClickListener(this);
        tbRep.setOnCheckedChangeListener(this);
        tbShuf.setOnCheckedChangeListener(this);
        seekBar.setOnSeekBarChangeListener(this);
        asyncPlay = new AsyncPlay();
        asyncPlay.execute();
    }

    @Override
    public void onStop() {
        asyncPlay.cancel(true);
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        comm.song_operations(v.getId());
    }

    public void playPause(String status) {
        if (status.contentEquals("play"))
            bPlay.setBackgroundResource(R.drawable.custom_pause);
        else
            bPlay.setBackgroundResource(R.drawable.custom_play);
    }
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        comm.playback_mode(compoundButton.getId(), b);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (b)
            comm.set_progress(i);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        comm.set_volume(0);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        comm.set_volume(-1);
    }

    public class AsyncPlay extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            seekBar.setMax(comm.get_song().getDuration());
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (comm.get_song().isPlaying()) {
                    seekBar.setProgress(comm.get_song().getCurrentPosition());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int elapsed = comm.get_song().getCurrentPosition() / 1000;
                            int remaining = (comm.get_song().getDuration() - comm.get_song().getCurrentPosition()) / 1000;
                            tvElapsed.setText(get_minutes(elapsed) + ":" + get_seconds(elapsed));
                            tvRemaining.setText("-" + get_minutes(remaining) + ":" + get_seconds(remaining));
                        }
                    });
                }
                if (isCancelled()) break;
            }
            return null;
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
    }
}
