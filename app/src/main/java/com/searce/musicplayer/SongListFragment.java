package com.searce.musicplayer;

import android.app.Fragment;
import android.content.Context;
import android.graphics.PathMeasure;
import android.graphics.Typeface;
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
    ArrayList<Song> songFiles;
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
        songId = comm.get_song_id();
        songAdapter = new SongListAdapter(songFiles);
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
        ArrayList<Song> songs;


        SongListAdapter(ArrayList<Song> songs) {
            this.songs = songs;
        }

        @Override
        public int getCount() {
            return songs.size();
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
            if (songs.get(i).getTitle().length() > 25)
                viewHolder.titleHolder.setText(songs.get(i).getTitle().substring(0, 25) + "...");
            else
                viewHolder.titleHolder.setText(songs.get(i).getTitle());
            if (songs.get(i).getArtist().length() > 30)
                viewHolder.artistHolder.setText(songs.get(i).getArtist().substring(0, 30) + "...");
            else
                viewHolder.artistHolder.setText(songs.get(i).getArtist());
            viewHolder.durationHolder.setText(songs.get(i).getDuration());

            if (songs.get(i).getId() == songFiles.get(songId).getId()) {
                viewHolder.imageHolder.setImageResource(R.drawable.playing_icon);
            } else
                viewHolder.imageHolder.setImageResource(R.drawable.song_icon);
            return view;
        }
    }
}
