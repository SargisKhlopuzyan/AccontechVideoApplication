package com.example.sargiskh.accontechvideoapplication.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.example.sargiskh.accontechvideoapplication.EventMessage;
import com.example.sargiskh.accontechvideoapplication.R;
import com.example.sargiskh.accontechvideoapplication.helpers.Constants;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class VideoDownloaderService extends IntentService {

    public static String VIDEOS_NAME_LIST = "VIDEOS_NAME_LIST";

    private ArrayList<String> videosNameToDownload = new ArrayList<>();
    private String cacheDir = "";
    private File rootFile;

    public VideoDownloaderService() {
        super("VideoDownloaderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e("LOG_TAG", "onHandleIntent");

        createRootDirectory();

        Bundle bundle = intent.getExtras();
        videosNameToDownload = bundle.getStringArrayList(VIDEOS_NAME_LIST);
        Log.e("LOG_TAG", "videosNameToDownload: " + videosNameToDownload.toString());

        if (videosNameToDownload == null) {
            return;
        }

        for (int i = 0; i < videosNameToDownload.size(); i++) {
            Log.e("LOG_TAG", "videoName: " + videosNameToDownload.get(i));
            downloadFile(videosNameToDownload.get(i));
        }
    }

    private void createRootDirectory() {
        cacheDir = Environment.getExternalStorageDirectory() + File.separator + Constants.CACHE_FOLDER_NAME;
        rootFile = new File(cacheDir);
        rootFile.mkdir();
    }

    public void downloadFile(String videoName) {

        try {
            String videoURL = Constants.BASE_URL + videoName;

            URL url = new URL(videoURL);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();

            FileOutputStream f = new FileOutputStream(new File(rootFile, videoName));
            InputStream in = c.getInputStream();
            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = in.read(buffer)) > 0) {
                f.write(buffer, 0, len1);
            }
            f.close();

            Log.e("LOG_TAG", "OK");
        } catch (IOException e) {
            Log.e("LOG_TAG", "IOException: " + e.toString());
        }

        EventBus.getDefault().post(new EventMessage(getString(R.string.at_list_one_video_is_downloaded)));
    }

}