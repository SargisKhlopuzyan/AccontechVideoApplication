package com.example.sargiskh.accontechvideoapplication;

import java.util.ArrayList;

/**
 * Created by SargisKh on 9/13/2017.
 */

public class EventMessage {

    private ArrayList<String> videosNames = new ArrayList<>();

    private boolean isConnectionError = false;
    private boolean isLoadedVideosNames = false;
    private boolean cachingCompleted = false;
    private String loadedVideoName = null;

    public EventMessage(String loadedVideoName, boolean cachingCompleted, boolean isLoadedVideosNames) {
        this.loadedVideoName = loadedVideoName;
        this.cachingCompleted = cachingCompleted;
        this.isLoadedVideosNames = isLoadedVideosNames;
        this.isConnectionError = false;
    }

    public EventMessage(ArrayList<String> videosNames, boolean isLoadedVideosNames) {
        this.videosNames = videosNames;
        this.isLoadedVideosNames = isLoadedVideosNames;
        this.cachingCompleted = false;
        this.isConnectionError = false;
    }

    public EventMessage(boolean isConnectionError) {
        this.isConnectionError = isConnectionError;
    }

    public boolean isLoadedVideosNames() {
        return isLoadedVideosNames;
    }

    public String getLoadedVideoName() {
        return loadedVideoName;
    }

    public boolean isCachingCompleted() {
        return cachingCompleted;
    }

    public ArrayList<String> getVideosNames() {
        return videosNames;
    }

    public boolean isConnectionError() {
        return isConnectionError;
    }
}
