package com.searce.musicplayer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by root on 5/9/14.
 */
public class TitleFrag extends Fragment implements View.OnClickListener {
    TextView tvTitle;
    Button bList;
    Communicator comm;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_title,container,false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        comm = (Communicator) getActivity();
        tvTitle = (TextView) getActivity().findViewById(R.id.tvSongTitle);
        bList = (Button) getActivity().findViewById(R.id.bBrowse);
        bList.setOnClickListener(this);
    }

    public void changeTitle(String newTitle){
        tvTitle.setText(newTitle);
    }

    public void changeAlbum(String newAlbum) {
        tvTitle.setText(newAlbum);
    }

    public void changeArtist(String newArtist) {
        tvTitle.setText(newArtist);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bBrowse:
                comm.show_list();
        }
    }
}
