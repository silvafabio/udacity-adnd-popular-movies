/*
 * Copyright (C) 2016 Fabio Luis
 */

package br.com.fabioluis.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * This Class encapsulates the most commonly used methods
 */

public class Utils {

    public static boolean isOnLine(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        } else {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return ((networkInfo != null) && (networkInfo.isConnected()));
        }
    }
}
