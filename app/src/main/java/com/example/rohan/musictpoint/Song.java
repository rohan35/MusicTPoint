package com.example.rohan.musictpoint;

/**
 * Created by Rohan on 11/24/2016.
 */

public class Song {
    private String sid;
    private String sartist;
    private String stitle;
    private String mPath;
    public Song(String id,String title,String path)

    {
        sid=id;
        stitle=title;

        mPath=path;
    }

    public String getId()
    {
        return sid;

    }
    public String getTitle()
    {
        return stitle;
    }
    public String getArtist()
    {
        return sartist;

    }
    public String getPath()
    {
        return mPath;
    }

}
