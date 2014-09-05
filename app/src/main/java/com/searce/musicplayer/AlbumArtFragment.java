package com.searce.musicplayer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by root on 5/9/14.
 */
public class AlbumArtFragment extends Fragment {
    TextView tvStatus;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_albumart,container,false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tvStatus = (TextView) getActivity().findViewById(R.id.tvStatus);
    }

    public void changeStatus(String newStatus){
        tvStatus.setText(newStatus);
    }
}
