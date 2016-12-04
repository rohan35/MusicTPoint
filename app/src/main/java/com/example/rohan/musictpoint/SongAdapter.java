package com.example.rohan.musictpoint;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static android.R.attr.value;
import static java.util.Collections.addAll;

/**
 * Created by Rohan on 11/24/2016.
 */

public class SongAdapter extends BaseAdapter {
    private ArrayList<Song> songs = new ArrayList<Song>();
    ;
    private ArrayList<Song> songs2 = new ArrayList<Song>();
    ;

    private LayoutInflater songInf;

    public SongAdapter(Context c, ArrayList<Song> theSongs) {
        songs = theSongs;
        songInf = LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout songLay = (LinearLayout) songInf.inflate(R.layout.list, parent, false);

        TextView songView = (TextView) songLay.findViewById(R.id.songTitle);

        ImageView image2 = (ImageView) songLay.findViewById(R.id.image);
        Song currSong = songs.get(position);
        songView.setText(currSong.getTitle());
        Picasso.with(songLay.getContext()).load(currSong.getPath()).into(image2);
        songLay.setTag(position);
        return songLay;
    }

}

