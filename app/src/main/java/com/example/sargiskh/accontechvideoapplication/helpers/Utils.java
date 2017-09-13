package com.example.sargiskh.accontechvideoapplication.helpers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by sargiskh on 9/13/2017.
 */

public class Utils {

    public static boolean doesUserHavePermission(Context context) {
        int resultWrite = context.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int resultRead = context.checkCallingOrSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        return  resultWrite == PackageManager.PERMISSION_GRANTED && resultRead == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

}
