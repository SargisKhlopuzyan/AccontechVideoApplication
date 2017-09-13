package com.example.sargiskh.accontechvideoapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.util.ArraySet;
import android.support.v7.app.AppCompatActivity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.sargiskh.accontechvideoapplication.helpers.Constants;
import com.example.sargiskh.accontechvideoapplication.helpers.Utils;
import com.example.sargiskh.accontechvideoapplication.services.VideoDownloaderService;
import com.example.sargiskh.accontechvideoapplication.services.VideosNameDownloaderService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

public class VideoViewActivity extends AppCompatActivity {

    private static String CURRENT_VIDEO_INDEX = "CURRENT_VIDEO_INDEX";
    private static String PLAYED_VIDEOS_COUNT = "PLAYED_VIDEOS_COUNT";
    private static String PLAYED_POSITION = "PLAYED_POSITION";
    private static String IS_PLAYING = "IS_PLAYING";

    EventBus myEventBus = EventBus.getDefault();

    private ProgressBar progressBar;
    private VideoView videoView;
    private MediaController mediaControls;

    private ArrayList<String> loadedVideosList = new ArrayList<>();
    private ArrayList<String> cachedVideosList = new ArrayList<>();
    private int currentVideoIndex = 0;
    private int playedVideosCount = 0;

    private void todoFunction() {
        loadedVideosList.add("videoplayback%20(1).mp4");
        loadedVideosList.add("1335026_10201324763394940_3213_n.mp4");
        loadedVideosList.add("13523484_518658811668898_1696164336_n.mp4");
        loadedVideosList.add("13949747_1158791877513554_1053660187_n.mp4");
        loadedVideosList.add("videoplayback.mp4");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);

        getVideosNamesInCachedDirectory();

        todoFunction();

        requestPermissions();

        findViews();

        setupMediaController();

        if (savedInstanceState != null) {
            Log.e("LOG_TAG", "savedInstanceState");
            currentVideoIndex = savedInstanceState.getInt(CURRENT_VIDEO_INDEX);
            playedVideosCount = savedInstanceState.getInt(PLAYED_VIDEOS_COUNT);
            int playedPosition = savedInstanceState.getInt(PLAYED_POSITION);
            boolean isPlaying = savedInstanceState.getBoolean(IS_PLAYING);

            if (isVideoCached(loadedVideosList.get(currentVideoIndex))) {
                playVideoFromCache(loadedVideosList.get(currentVideoIndex), isPlaying, playedPosition);
            } else {
                showProgressBar(true);
                //TODO
                playVideo();
            }

        } else {
            Log.e("LOG_TAG", "savedInstanceState == null");
            loadVideosName();
            //TODO
            todoFunction();
        }

        // implement on completion listener on video view
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                ++currentVideoIndex;
                ++playedVideosCount;
                playVideo();
                Toast.makeText(getApplicationContext(), "Thank You...!!!", Toast.LENGTH_LONG).show(); // display a toast when an video is completed
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(getApplicationContext(), "Oops An Error Occur While Playing Video...!!!", Toast.LENGTH_LONG).show(); // display a toast when an error is occured while playing an video
                return false;
            }
        });
    }

    private boolean isNewVideosAvailable() {
        if (getNotLoadedVideosNamesList().size() == 0) {
            return false;
        }
        return true;
    }

    private void removeUnnecessaryCachedVideos() {
        ArrayList<String> unnecessaryArrayList = getUnnecessaryVideosNamesList();
        for (String name : unnecessaryArrayList) {
            String videoAddress = Constants.CACHE_PATH + name;
            File videoFile = new File(videoAddress);
            if (videoFile.exists()) {
                if (videoFile.delete()) {
                    cachedVideosList.remove(name);
                } else {
                    Log.e("LOG_TAG", "Can not delete video");
                }
            }
        }
    }

    private ArrayList<String> getUnnecessaryVideosNamesList() {

        ArrayList<String> notNecessaryVideosNamesList = new ArrayList<>();
        Set<String> loadedVideoListSet = new ArraySet<>();

        for (String loadedName : loadedVideosList) {
            loadedVideoListSet.add(loadedName);
        }

        for (String cachedName : cachedVideosList) {
            if (loadedVideoListSet.add(cachedName)) {
                notNecessaryVideosNamesList.add(cachedName);
            }
        }

        return notNecessaryVideosNamesList;
    }

    private ArrayList<String> getNotLoadedVideosNamesList() {
        ArrayList<String> uncachedVideoList = new ArrayList<>();
        Set<String> uncachedVideoListSet = new ArraySet<>();

        for (String cachedName : cachedVideosList) {
            uncachedVideoListSet.add(cachedName);
        }

        for (String loadedVideoName : loadedVideosList) {
            if (uncachedVideoListSet.add(loadedVideoName)) {
                uncachedVideoList.add(loadedVideoName);
            }
        }

        return uncachedVideoList;
    }

    private void playVideo() {

        if (currentVideoIndex == cachedVideosList.size()) {

            if (currentVideoIndex == playedVideosCount) {
                playedVideosCount = 0;
                currentVideoIndex = 0;
                loadVideosName();
            } else {
                playedVideosCount = 0;
                currentVideoIndex = 0;
                playVideo();
            }
        } else  {
            if (isVideoCached(cachedVideosList.get(currentVideoIndex))) {
                playVideoFromCache(cachedVideosList.get(currentVideoIndex));
            } else {
                ++currentVideoIndex;
                playVideo();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void messageEventFromService(EventMessage event){
        String notification = event.getNotification();
        Log.e("LOG_TAG", "notification: " + notification);
        if (notification.equalsIgnoreCase(getString(R.string.videos_names_are_downloaded))) {

            if (getUnnecessaryVideosNamesList().size() != 0) {
                removeUnnecessaryCachedVideos();
            }
            //TODO
            if (isNewVideosAvailable()) {
                loadVideos(getNotLoadedVideosNamesList());
            }

        } else if (notification.equalsIgnoreCase(getString(R.string.at_list_one_video_is_downloaded))) {
            currentVideoIndex = 0;
            playedVideosCount = 0;
            playVideo();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private void requestPermissions() {
        if (!Utils.doesUserHavePermission(this)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    private void findViews() {
        // Find your VideoView in your video_main.xml layout
        videoView = (VideoView) findViewById(R.id.video_view);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
    }

    private void setupMediaController() {
        if (mediaControls == null) {
            // create an object of media controller class
            mediaControls = new MediaController(VideoViewActivity.this);
            mediaControls.setAnchorView(videoView);
        }

        // set the media controller for video view
        videoView.setMediaController(mediaControls);
    }

    private boolean isVideoCached(String videoName) {
        String videoAddress = Constants.CACHE_PATH + videoName;
        File  videoFile = new File(videoAddress);
        if (videoFile.exists())
            return true;
        return false;
    }

    private void getVideosNamesInCachedDirectory() {

        String cacheDir = Environment.getExternalStorageDirectory() + File.separator + Constants.CACHE_FOLDER_NAME;
        File rootFile = new File(cacheDir);

        cachedVideosList.clear();
        if (!rootFile.isDirectory()) {
            return;
        }

        File[] files = rootFile.listFiles();
        for (File file : files) {
            if (!file.isDirectory()) {
                if(file.getName().endsWith(".mp4")){
                    cachedVideosList.add(file.getName());
                }
            }
        }
    }

    private void playVideoFromCache(String videoName) {
        String videoAddress = Constants.CACHE_PATH + videoName;

        // set the path for the video view
        videoView.setVideoPath(videoAddress);

        // start a video
        videoView.start();
    }

    private void playVideoFromCache(String videoName, boolean isPlaying, int currentPosition) {

        String videoAddress = Constants.CACHE_PATH + videoName;

        // set the path for the video view
        videoView.setVideoPath(videoAddress);

        videoView.seekTo(currentPosition);
        if (isPlaying) {
            // start a video
            videoView.start();
        }
    }

    private void showProgressBar(boolean isVisible) {
        progressBar.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    private void loadVideosName() {

//        Bundle bundle = new Bundle();
//        bundle.putStringArrayList(VideoDownloaderService.VIDEOS_NAME_LIST, videosToDownload);
        Intent intent = new Intent(this, VideosNameDownloaderService.class);
//        intent.putExtras(bundle);
        startService(intent);
    }

    private void loadVideos(ArrayList<String> videosToDownload) {

        Bundle bundle = new Bundle();
        bundle.putStringArrayList(VideoDownloaderService.VIDEOS_NAME_LIST, videosToDownload);

        Intent intent = new Intent(this, VideoDownloaderService.class);
        intent.putExtras(bundle);

        startService(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURRENT_VIDEO_INDEX, currentVideoIndex);
        outState.putInt(PLAYED_VIDEOS_COUNT, playedVideosCount);
        outState.putInt(PLAYED_POSITION, videoView.getCurrentPosition());
        outState.putBoolean(IS_PLAYING, videoView.isPlaying());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    loadVideos(loadedVideosList);
                } else {
                    // permission denied
                    Toast.makeText(this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
}
