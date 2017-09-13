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

    public static String CURRENT_PLAYING_VIDEO_INDEX = "CURRENT_PLAYING_VIDEO_INDEX";
    public static String LOADED_VIDEOS_LIST = "LOADED_VIDEOS_LIST";
    public static String VIDEO_PLAYED_DURATION = "VIDEO_PLAYED_DURATION";
    public static String PROGRESS_BAR_VISIBILITY = "PROGRESS_BAR_VISIBILITY";
    public static String IS_PLAYING = "IS_PLAYING";
    public static String VIDEOS_NAME_LIST = "VIDEOS_NAME_LIST";
}
