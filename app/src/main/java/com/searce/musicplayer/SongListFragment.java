package com.searce.musicplayer;

import android.app.Fragment;
import android.content.Context;
import android.graphics.PathMeasure;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by debowin on 7/9/14.
 */
public class SongListFragment extends Fragment implements AdapterView.OnItemClickListener {
    ArrayList<String> songFiles;
    ArrayList<String> songTitles;
    ArrayList<String> songArtists;
    ArrayList<String> songDurations;
    SongListAdapter songAdapter;
    ListView lvSongs;
    Communicator comm;
    int songId;

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
        lvSongs.setScrollingCacheEnabled(false);
        lvSongs.setOnItemClickListener(this);

        songFiles = comm.get_song_list();
        songTitles = comm.get_song_titles();
        songArtists = comm.get_song_artists();
        songDurations = comm.get_song_durations();
        songId = comm.get_song_id();
        songAdapter = new SongListAdapter(songFiles, songTitles, songArtists, songDurations);
        lvSongs.setAdapter(songAdapter);
        Log.e("MP3 files found...", String.valueOf(songFiles.size()));
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        comm.open_song(i);
    }

    static class ViewHolderItem {
        TextView titleHolder;
        TextView artistHolder;
        TextView durationHolder;
        ImageView imageHolder;
    }

    public class SongListAdapter extends BaseAdapter {
        ArrayList<String> paths;
        ArrayList<String> titles;
        ArrayList<String> artists;
        ArrayList<String> durations;


        SongListAdapter(ArrayList<String> paths, ArrayList<String> titles, ArrayList<String> artists, ArrayList<String> durations) {
            this.paths = paths;
            this.titles = titles;
            this.artists = artists;
            this.durations = durations;
        }

        @Override
        public int getCount() {
            return paths.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolderItem viewHolder;
            if (view == null) {
//                Log.e("Dayem","View was null.");
                // Inflate the view
                LayoutInflater inflater = getActivity().getLayoutInflater();
                view = inflater.inflate(R.layout.list_item_song, viewGroup, false);
                //Set up the View Holder
                viewHolder = new ViewHolderItem();
                viewHolder.imageHolder = (ImageView) view.findViewById(R.id.ivListItem);
                viewHolder.titleHolder = (TextView) view.findViewById(R.id.tvSongTitle_ListItem);
                viewHolder.artistHolder = (TextView) view.findViewById(R.id.tvArtist_ListItem);
                viewHolder.durationHolder = (TextView) view.findViewById(R.id.tvDuration);
                //Store the holder with the view
                view.setTag(viewHolder);
            } else {
//                Log.e("Awesome","Got the view.");
                viewHolder = (ViewHolderItem) view.getTag();
            }

            viewHolder.titleHolder.setText(titles.get(i));
            viewHolder.artistHolder.setText(artists.get(i));
            viewHolder.durationHolder.setText(durations.get(i));
            if (paths.get(i).contentEquals(songFiles.get(songId))) {
                viewHolder.imageHolder.setImageResource(R.drawable.playing_icon);
            } else
                viewHolder.imageHolder.setImageResource(R.drawable.song_icon);
            return view;
        }
    }
}
