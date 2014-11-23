package com.searce.musicplayer;

import android.app.Fragment;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
    TextView tvElapsed, tvRemaining, tvDuration;
    TextView tvTitle;
    TextView tvAlbum;
    TextView tvArtist;
    TextView tvIndex;
    Button bList, bVolume;
    Communicator comm;
    AsyncPlay asyncPlay;
    int new_progress;
    boolean skip_progress_updates;

//    public class OnSwipeTouchListener implements View.OnTouchListener{
//        private final GestureDetector gestureDetector;
//        public OnSwipeTouchListener(Context ctx) {
//            gestureDetector = new GestureDetector(ctx,new GestureListener());
//        }
//
//        @Override
//        public boolean onTouch(View view, MotionEvent motionEvent) {
//            return false;
//        }
//    }

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
        tvDuration = (TextView) getActivity().findViewById(R.id.tvDuration_Player);
        tvTitle = (TextView) getActivity().findViewById(R.id.tvSongTitle_TitleFrag);
        tvAlbum = (TextView) getActivity().findViewById(R.id.tvAlbum_TitleFrag);
        tvArtist = (TextView) getActivity().findViewById(R.id.tvArtist_TitleFrag);
        tvIndex = (TextView) getActivity().findViewById(R.id.tvIndex);
        bList = (Button) getActivity().findViewById(R.id.bBrowse);
        bVolume = (Button) getActivity().findViewById(R.id.bVolume);
        ivAlbumArt = (ImageView) getActivity().findViewById(R.id.ivAlbumArt);
        bList.setOnClickListener(this);
        bPlay.setOnClickListener(this);
        bPrev.setOnClickListener(this);
        bNext.setOnClickListener(this);
        bVolume.setOnClickListener(this);

        tbRep.setOnCheckedChangeListener(this);
        tbShuf.setOnCheckedChangeListener(this);
        seekBar.setOnSeekBarChangeListener(this);
        ivAlbumArt.setOnTouchListener(new OnSwipeTouchListener(getActivity().getBaseContext()) {
            // For next/prev operation based on swipes.
            @Override
            public void onSwipeRight() {
                comm.song_operations(bPrev.getId());
            }

            @Override
            public void onSwipeLeft() {
                comm.song_operations(bNext.getId());
            }
        });
        tvTitle.setSelected(true);
        tvAlbum.setSelected(true);
        tvArtist.setSelected(true);
        updateAlbumArt();
        updateTags();
        setMaxDuration(comm.get_duration());
    }

    @Override
    public void onStop() {
        asyncPlay.cancel(true);
        super.onStop();
    }

    @Override
    public void onResume() {
        asyncPlay = new AsyncPlay();
        asyncPlay.execute();
        if (!comm.is_playing()) {
            updateTimers(comm.get_elapsed());
            bPlay.setBackgroundResource(R.drawable.custom_play);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        asyncPlay.cancel(true);
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        comm.song_operations(v.getId());
        if (comm.is_playing())
            bPlay.setBackgroundResource(R.drawable.custom_pause);
        else
            bPlay.setBackgroundResource(R.drawable.custom_play);
        seekBar.setProgress(comm.get_elapsed());
        updateTimers(comm.get_elapsed());
    }
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        comm.playback_mode(compoundButton.getId(), b);
    }

    @Override
    public void onProgressChanged(final SeekBar seekBar, int i, boolean b) {
        if (b) {
            new_progress = i;
            updateTimers(i);
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
            seekBar.setMax(comm.get_duration());
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while (true) {
                if (skip_progress_updates) {
                    continue;
                }
                // Revert to original colors on playing.
                if (comm.is_playing()) {
                    if (tvElapsed.getCurrentTextColor() == getResources().getColor(R.color.white)) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvElapsed.setTextColor(getResources().getColor(R.color.holo));
                            }
                        });
                    }
                    seekBar.setProgress(comm.get_elapsed());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateTimers(comm.get_elapsed());
                        }
                    });
                } else {
                    //Blinking effect on pause.
                    if (tvElapsed.getCurrentTextColor() == getResources().getColor(R.color.white)) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvElapsed.setTextColor(getResources().getColor(R.color.holo));
                            }
                        });
                    } else if (tvElapsed.getCurrentTextColor() == getResources().getColor(R.color.holo)) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvElapsed.setTextColor(getResources().getColor(R.color.white));
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

    private void updateTimers(int progress) {
        int elapsed = progress / 1000;
        int remaining = (comm.get_duration() - progress) / 1000;
        tvElapsed.setText(" " + get_minutes(elapsed) + ":" + get_seconds(elapsed) + " ");
        tvRemaining.setText("- " + get_minutes(remaining) + ":" + get_seconds(remaining) + " ");
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
        int index = comm.get_song_id() + 1;
        tvIndex.setText(" " + index + " of " + comm.get_song_list().size() + " ");
        tvDuration.setText(comm.get_song_list().get(comm.get_song_id()).getDuration());
    }

    public void setMaxDuration(int duration) {
        seekBar.setMax(duration);
    }
}
