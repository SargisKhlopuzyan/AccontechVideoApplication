package com.example.sargiskh.accontechvideoapplication.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.example.sargiskh.accontechvideoapplication.EventMessage;
import com.example.sargiskh.accontechvideoapplication.R;
import com.example.sargiskh.accontechvideoapplication.helpers.Constants;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class VideosNameDownloaderService extends IntentService {


    public VideosNameDownloaderService() {
        super("VideosNameDownloaderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e("LOG_TAG", "VideosNameDownloaderService onHandleIntent");
        downloadVideosNames();
    }

    public void downloadVideosNames() {

        HttpURLConnection c = null;

        try {
            StringBuilder result = new StringBuilder();

            String videoURL = Constants.BASE_URL;

            URL url = new URL(videoURL);
            c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();

            Log.e("LOG_TAG", "VideosNameDownloaderService 01");
            InputStream in = new BufferedInputStream(c.getInputStream());

            Log.e("LOG_TAG", "VideosNameDownloaderService 02");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            Log.e("LOG_TAG", "VideosNameDownloaderService 03");
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            Log.e("LOG_TAG", "VideosNameDownloaderService result: " + result.toString());

        } catch (IOException e) {
            Log.e("LOG_TAG", "VideosNameDownloaderService IOException: " + e.toString());
        } finally {
            Log.e("LOG_TAG", "VideosNameDownloaderService finally");
            if (c != null) {
                c.disconnect();
            }
        }

        EventBus.getDefault().post(new EventMessage(getString(R.string.videos_names_are_downloaded)));
    }

}