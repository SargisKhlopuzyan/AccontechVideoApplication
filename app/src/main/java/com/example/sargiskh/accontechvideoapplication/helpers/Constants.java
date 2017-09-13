package com.example.sargiskh.accontechvideoapplication.helpers;

import android.os.Environment;

import java.io.File;

/**
 * Created by sargiskh on 9/13/2017.
 */

public class Constants {

    public static String BASE_URL = "http://93.94.217.144:8080/videos/";

    public static String CACHE_FOLDER_NAME = "AccontechCachedVideo";

    public static String CACHE_PATH = Environment.getExternalStorageDirectory() + File.separator + CACHE_FOLDER_NAME + File.separator;

}
