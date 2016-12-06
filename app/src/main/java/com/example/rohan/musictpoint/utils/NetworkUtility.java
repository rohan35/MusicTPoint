package com.example.rohan.musictpoint.utils;

import android.content.Context;


public class NetworkUtility {

    public static boolean isNetworkConnected(Context context) {
        final NetworkUtility connectivityManager = ((NetworkUtility) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
