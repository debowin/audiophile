package com.searce.musicplayer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ToggleButton;

/**
 * Created by root on 4/9/14.
 */
public class PlayerFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener{
    Button bPlay, bPrev, bNext;
    ToggleButton tbRep, tbShuf;
    Communicator comm;

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
        bPlay.setOnClickListener(this);
        bPrev.setOnClickListener(this);
        bNext.setOnClickListener(this);
        tbRep.setOnCheckedChangeListener(this);
        tbShuf.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        comm.tell_parent(v.getId());
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        comm.tell_parent(compoundButton.getId(),b);
    }
}
