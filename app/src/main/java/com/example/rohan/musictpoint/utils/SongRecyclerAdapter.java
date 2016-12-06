package com.example.rohan.musictpoint.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.rohan.musictpoint.R;
import com.example.rohan.musictpoint.model.Song;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SongRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<Song> songs = new ArrayList<Song>();
    private LayoutInflater songInf;
    private Context context;

    SongRecyclerAdapter(Context c, ArrayList<Song> theSongs) {
        songs = theSongs;
        songInf = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        context = c;
    }

    @Override
    public int getItemCount() {
        return songs != null ? songs.size() : 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = songInf.inflate(R.layout.list, parent, false);
        final ItemViewHolder itemViewHolder = new ItemViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Song song = songs.get(itemViewHolder.getAdapterPosition());
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + song.getId()));
                context.startActivity(intent);
            }
        });
        return itemViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            Song currSong = songs.get(position);
            itemViewHolder.songView.setText(currSong.getTitle());
            Picasso.with(context).load(currSong.getPath()).into(itemViewHolder.image2);
        }
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView image2;
        private TextView songView;

        ItemViewHolder(View itemView) {
            super(itemView);
            songView = (TextView) itemView.findViewById(R.id.songTitle);
            image2 = (ImageView) itemView.findViewById(R.id.image);
        }
    }
}