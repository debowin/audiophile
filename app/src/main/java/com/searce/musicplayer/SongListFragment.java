package com.searce.musicplayer;

import android.app.Fragment;
import android.graphics.PathMeasure;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by debowin on 7/9/14.
 */
public class SongListFragment extends Fragment implements AdapterView.OnItemClickListener {
    ArrayList<String> songFiles;
    ArrayAdapter<String> songAdapter;
    ListView lvSongs;
    Communicator comm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_songlist, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        comm = (Communicator) getActivity();
        lvSongs = (ListView) getActivity().findViewById(R.id.lvSongs);
        lvSongs.setOnItemClickListener(this);

        songFiles = comm.get_song_list();
        songAdapter = new ArrayAdapter<String>(getActivity().getBaseContext(), R.layout.list_item_song, songFiles);
        lvSongs.setAdapter(songAdapter);
        Log.e("MP3 files found...", String.valueOf(songFiles.size()));
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        comm.open_song(i);
    }
}
