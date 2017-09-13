package com.example.sargiskh.accontechvideoapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.sargiskh.accontechvideoapplication.helpers.Constants;
import com.example.sargiskh.accontechvideoapplication.helpers.Utils;
import com.example.sargiskh.accontechvideoapplication.services.VideoDownloaderService;
import com.example.sargiskh.accontechvideoapplication.services.VideosNamesDownloaderService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;

public class VideoViewActivity extends AppCompatActivity {

    private VideoView videoView;
    private SeekBar seekBar;
    private ProgressBar progressBar;
    private Button permissionButton;
    private View internetConnectionErrorView;

    private MediaController mediaControls;

    private ArrayList<String> loadedVideosNamesList = new ArrayList<>();
    private ArrayList<String> cachedVideosList = new ArrayList<>();

    private boolean isCachingCompleted = true;
    private boolean isVideoPlaying = false;
    private int currentPlayingVideoIndex = 0;

    EventBus eventBus = EventBus.getDefault();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFullScreen();
        setContentView(R.layout.activity_video_view);

        getCachedVideosNames();

        findViews();

        setupMediaController();

        handleSavedInstanceState(savedInstanceState);

        setListeners();
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(Constants.CURRENT_PLAYING_VIDEO_INDEX, currentPlayingVideoIndex);
        outState.putInt(Constants.VIDEO_PLAYED_DURATION, videoView.getCurrentPosition());
        outState.putBoolean(Constants.IS_PLAYING, videoView.isPlaying());
        outState.putInt(Constants.PROGRESS_BAR_VISIBILITY, progressBar.getVisibility());
        outState.putStringArrayList(Constants.LOADED_VIDEOS_LIST, loadedVideosNamesList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    permissionButton.setVisibility(View.GONE);
                    loadVideosNames();
                } else {
                    permissionButton.setVisibility(View.VISIBLE);
                    // permission denied
                    Toast.makeText(this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void messageEventFromService(EventMessage event){

        if (event.isConnectionError()) {
            return;
        }

        boolean isLoadingVideosNames = event.isLoadedVideosNames();

        if (isLoadingVideosNames) {
            loadedVideosNamesList.clear();
            for (String name : event.getVideosNames()) {
                loadedVideosNamesList.add(name);
            }
            removeUnnecessaryCachedVideos();

            if (Utils.doesUserHavePermission(this)) {
                loadVideos(loadedVideosNamesList);
            }

        } else {
            String cachedVideo = event.getLoadedVideoName();
            if (cachedVideo != null) {
                if (!cachedVideosList.contains(cachedVideo)) {
                    cachedVideosList.add(cachedVideo);
                }
            }

            if (cachedVideosList.size() > 0) {
                progressBar.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.VISIBLE);
            }

            isCachingCompleted = event.isCachingCompleted();
            if (!isVideoPlaying) {
                isVideoPlaying = true;
                playVideo();
            }
        }
    }

    // Requests permissions
    private void requestPermissions() {
        if (!Utils.doesUserHavePermission(this)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    private void handleSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {

            if (!Utils.doesUserHavePermission(this)) {
                permissionButton.setVisibility(View.VISIBLE);
            } else {
                permissionButton.setVisibility(View.GONE);
            }

            currentPlayingVideoIndex = savedInstanceState.getInt(Constants.CURRENT_PLAYING_VIDEO_INDEX);
            loadedVideosNamesList = savedInstanceState.getStringArrayList(Constants.LOADED_VIDEOS_LIST);
            int videoPlayedDuration = savedInstanceState.getInt(Constants.VIDEO_PLAYED_DURATION);
            boolean isPlaying = savedInstanceState.getBoolean(Constants.IS_PLAYING);

            int isVisible = savedInstanceState.getInt(Constants.PROGRESS_BAR_VISIBILITY) == View.VISIBLE ? View.VISIBLE : View.GONE;
            progressBar.setVisibility(isVisible);

            if (currentPlayingVideoIndex < cachedVideosList.size()) {
                String name = cachedVideosList.get(currentPlayingVideoIndex);
                if (name != null && isVideoCached(name)) {
                    playVideoFromCache(name, isPlaying, videoPlayedDuration);
                }
            }
        } else  {
            if (!Utils.doesUserHavePermission(this)) {
                permissionButton.setVisibility(View.VISIBLE);
                requestPermissions();
            } else {
                permissionButton.setVisibility(View.GONE);
                loadVideosNames();
            }
        }
    }

    // Finds views
    private void findViews() {
        videoView = (VideoView) findViewById(R.id.video_view);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        permissionButton = (Button) findViewById(R.id.button_permission);
        internetConnectionErrorView = findViewById(R.id.internet_connection_error_view);
    }

    private void setListeners() {

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
//                audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
                Log.e("LOG_TAG", "progress: " + progress);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int isVisible = seekBar.getVisibility() ==  View.VISIBLE ? View.GONE : View.VISIBLE;
                seekBar.setVisibility(isVisible);
                return false;
            }
        });

        // implement on completion listener on video view
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playNextVideo();
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });
    }

    // Sets MediaController
    private void setupMediaController() {
        if (mediaControls == null) {
            // create an object of media controller class
            mediaControls = new MediaController(VideoViewActivity.this);
            mediaControls.setAnchorView(videoView);
        }

        // set the media controller for video view
        videoView.setMediaController(mediaControls);
    }

    private void playNextVideo() {
        ++currentPlayingVideoIndex;
        playVideo();
    }

    private void showInternetConnectionError() {
        internetConnectionErrorView.setVisibility(View.VISIBLE);
    }

    private void playVideo() {

        if (!Utils.isNetworkAvailable(this) && cachedVideosList.size() == 0) {
            showInternetConnectionError();
            return;
        }

        if (isCachingCompleted) {
            if (currentPlayingVideoIndex == cachedVideosList.size()) {
                currentPlayingVideoIndex = 0;
                loadVideosNames();
            } else {
                playVideoFromCache(cachedVideosList.get(currentPlayingVideoIndex));
            }

        } else {
            if (currentPlayingVideoIndex == cachedVideosList.size()) {
                currentPlayingVideoIndex = 0;
            }

            String name = cachedVideosList.get(currentPlayingVideoIndex);
            if (isVideoCached(name)) {
                playVideoFromCache(name);
            } else {
                ++currentPlayingVideoIndex;
                playVideo();
            }
        }
    }

    private boolean isVideoCached(String videoName) {
        String videoAddress = Constants.CACHE_PATH + videoName;
        File  videoFile = new File(videoAddress);
        if (videoFile.exists())
            return true;
        return false;
    }

    private void playVideoFromCache(String videoName) {
        if (progressBar.getVisibility() == View.VISIBLE) {
            progressBar.setVisibility(View.GONE);
        }
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

    private void getCachedVideosNames() {

        String cacheDir = Environment.getExternalStorageDirectory() + File.separator + Constants.CACHE_FOLDER_NAME;
        File rootFile = new File(cacheDir);

        cachedVideosList.clear();
        if (!rootFile.isDirectory()) {
            return;
        }

        File[] files = rootFile.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (!file.isDirectory()) {
                if(file.getName().endsWith(".mp4")){
                    cachedVideosList.add(file.getName());
                }
            }
        }
    }

    private void removeUnnecessaryCachedVideos() {

        getCachedVideosNames();

        for (String name : cachedVideosList) {
            if (!loadedVideosNamesList.contains(name)) {
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
    }

    private void loadVideosNames() {
        if (Utils.isNetworkAvailable(this)) {
            progressBar.setVisibility(View.VISIBLE);
            isVideoPlaying = false;
            Intent intent = new Intent(this, VideosNamesDownloaderService.class);
            startService(intent);
        } else {
            playVideo();
        }
    }

    private void loadVideos(ArrayList<String> videosToDownload) {
        isCachingCompleted = false;
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(Constants.VIDEOS_NAME_LIST, videosToDownload);
        Intent intent = new Intent(this, VideoDownloaderService.class);
        intent.putExtras(bundle);
        startService(intent);
    }

    public void permissionButtonClicked(View view) {
        if (!Utils.doesUserHavePermission(this)) {
            requestPermissions();
        }
    }

    public void internetConnectionErrorButtonClicked(View view) {
        if (Utils.isNetworkAvailable(this)) {
            internetConnectionErrorView.setVisibility(View.GONE);
            loadVideosNames();
        } else {
            internetConnectionErrorView.setVisibility(View.VISIBLE);
        }
    }
}
