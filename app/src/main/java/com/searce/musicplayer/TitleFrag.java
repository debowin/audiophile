package com.searce.musicplayer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by root on 5/9/14.
 */
public class TitleFrag extends Fragment implements View.OnClickListener {
    TextView tvTitle;
    TextView tvAlbum;
    TextView tvArtist;
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
        tvTitle = (TextView) getActivity().findViewById(R.id.tvSongTitle_TitleFrag);
        tvAlbum = (TextView) getActivity().findViewById(R.id.tvAlbum_TitleFrag);
        tvArtist = (TextView) getActivity().findViewById(R.id.tvArtist_TitleFrag);
        bList = (Button) getActivity().findViewById(R.id.bBrowse);
        bList.setOnClickListener(this);
        updateTags();
    }

    public void updateTags() {
        tvTitle.setText(comm.get_title());
        tvAlbum.setText(comm.get_album());
        tvArtist.setText(comm.get_artist());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bBrowse:
                comm.show_list();
        }
    }
}
