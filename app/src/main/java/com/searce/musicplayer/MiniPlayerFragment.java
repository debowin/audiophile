package com.searce.musicplayer;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * Created by debowin on 7/9/14.
 */
public class MiniPlayerFragment extends Fragment implements View.OnClickListener {
    RelativeLayout rlMiniPlayer;
    Communicator comm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_mini_player, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        comm = (Communicator) getActivity();
        rlMiniPlayer = (RelativeLayout) getActivity().findViewById(R.id.rlPlayer);
        rlMiniPlayer.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rlPlayer:
                comm.goToPlayer();
        }
    }
}
