package com.example.rohan.musictpoint.fetchers;

import com.example.rohan.musictpoint.model.Song;

import java.util.ArrayList;


public interface OnSongRetrieving {
    void retrievedSong(ArrayList<Song> songArrayList);
}
