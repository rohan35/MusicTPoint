package com.example.rohan.musictpoint.fetchers;

import com.example.rohan.musictpoint.model.Song;

import java.util.ArrayList;

/**
 * Created by anirudh.r on 04/12/16.
 */

public interface OnSongRetrieving {
    void retrievedSong(ArrayList<Song> songArrayList);
}
