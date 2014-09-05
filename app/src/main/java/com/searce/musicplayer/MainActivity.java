package com.searce.musicplayer;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

interface Communicator{
    public void tell_parent(int id);
}

public class MainActivity extends Activity implements Communicator{
    PlayerFragment playerFrag;
    AlbumArtFragment artFrag;
    TitleFrag titleFrag;
    FragmentManager manager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        if (savedInstanceState == null) {
            playerFrag = new PlayerFragment();
            artFrag = new AlbumArtFragment();
            titleFrag = new TitleFrag();
            manager = getFragmentManager();
            FragmentTransaction transaction =  manager.beginTransaction();
            transaction.add(R.id.container, titleFrag);
            transaction.add(R.id.container, artFrag);
            transaction.add(R.id.container, playerFrag);
            transaction.commit();
        }
    }

    public void toggleFullscreen(boolean fullscreen)
    {
        if (fullscreen)
            getActionBar().hide();
        else
            getActionBar().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.action_search:
                break;
            case R.id.action_settings:
                break;
            case R.id.action_about:
                Intent about = new Intent(MainActivity.this,About.class);
                startActivity(about);
                break;
            case R.id.action_exit:
                finish();
                break;
        }
        return false;
    }

    @Override
    public void tell_parent(int id) {
        switch(id){
            case R.id.bPlay:
                artFrag.changeStatus("Play Button Pressed!");
                break;
            case R.id.bNext:
                artFrag.changeStatus("Next Button Pressed!");
                break;
            case R.id.bPrev:
                artFrag.changeStatus("Previous Button Pressed!");
                break;
        }
    }
}
