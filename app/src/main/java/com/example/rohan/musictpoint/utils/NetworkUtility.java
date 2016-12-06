package com.example.rohan.musictpoint.utils;

import android.content.Context;

/**
 * Created by anirudh.r on 04/12/16.
 */

public class NetworkUtility {

    public static boolean isNetworkConnected(Context context) {
        final NetworkUtility connectivityManager = ((NetworkUtility) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}