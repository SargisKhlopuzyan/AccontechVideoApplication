package com.example.sargiskh.accontechvideoapplication.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.example.sargiskh.accontechvideoapplication.EventMessage;
import com.example.sargiskh.accontechvideoapplication.helpers.Constants;
import com.example.sargiskh.accontechvideoapplication.helpers.Utils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class VideoDownloaderService extends IntentService {

    private ArrayList<String> videosToDownload = new ArrayList<>();
    private String cacheDir = "";
    private File rootFile;

    public VideoDownloaderService() {
        super("VideoDownloaderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        createRootDirectory();

        Bundle bundle = intent.getExtras();
        videosToDownload = bundle.getStringArrayList(Constants.VIDEOS_NAME_LIST);

        if (videosToDownload == null) {
            return;
        }

        for (int i = 0; i < videosToDownload.size(); i++) {
            if (Utils.isNetworkAvailable(this)) {
                downloadFile(i);
            } else {
                EventBus.getDefault().post(new EventMessage(true));
            }
        }
    }

    private void createRootDirectory() {
        cacheDir = Environment.getExternalStorageDirectory() + File.separator + Constants.CACHE_FOLDER_NAME;
        rootFile = new File(cacheDir);
        rootFile.mkdir();
    }

    private String checkVideoNameSpelling(String name) {
        return name.replace(" ", "%20");
    }

    public void downloadFile(int i) {

        String originalVideoName = videosToDownload.get(i);

        if (!isVideoCached(originalVideoName)) {
            String videoName = checkVideoNameSpelling(originalVideoName);

            try {
                String videoURL = Constants.BASE_URL + videoName;

                URL url = new URL(videoURL);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.setDoOutput(true);
                c.connect();

                FileOutputStream f = new FileOutputStream(new File(rootFile, originalVideoName));
                InputStream in = c.getInputStream();
                byte[] buffer = new byte[1024];
                int len1 = 0;
                while ((len1 = in.read(buffer)) > 0) {
                    f.write(buffer, 0, len1);
                }
                f.close();
            } catch (FileNotFoundException e) {
                deleteVideo(originalVideoName);
                EventBus.getDefault().post(new EventMessage(true));
            } catch (ProtocolException e) {
                deleteVideo(originalVideoName);
                EventBus.getDefault().post(new EventMessage(true));
            } catch (MalformedURLException e) {
                deleteVideo(originalVideoName);
                EventBus.getDefault().post(new EventMessage(true));
            } catch (IOException e) {
                deleteVideo(originalVideoName);
                EventBus.getDefault().post(new EventMessage(true));
            }

            if (i == videosToDownload.size() - 1) {
                EventBus.getDefault().post(new EventMessage(originalVideoName, true, false));
            } else {
                EventBus.getDefault().post(new EventMessage(originalVideoName, false, false));
            }
        } else {
            if (i == videosToDownload.size() - 1) {
                EventBus.getDefault().post(new EventMessage(null, true, false));
            } else {
                EventBus.getDefault().post(new EventMessage(null, false, false));
            }
        }
    }

    private boolean isVideoCached(String videoName) {
        String videoAddress = Constants.CACHE_PATH + videoName;
        File videoFile = new File(videoAddress);
        if (videoFile.exists())
            return true;
        return false;
    }

    private void deleteVideo(String name) {
        String videoAddress = Constants.CACHE_PATH + name;
        File videoFile = new File(videoAddress);
        if (videoFile.exists()) {
            videoFile.delete();
        }
    }
}