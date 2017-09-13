package com.example.sargiskh.accontechvideoapplication;

import java.util.ArrayList;

/**
 * Created by SargisKh on 9/13/2017.
 */

public class EventMessage {

    private ArrayList<String> videosNames = new ArrayList<>();

    private boolean isLoadedVideosNames = false;
    private boolean cachingCompleted = false;
    private String loadedVideoName = null;

    public EventMessage(String loadedVideoName, boolean cachingCompleted, boolean isLoadedVideosNames) {
        this.loadedVideoName = loadedVideoName;
        this.cachingCompleted = cachingCompleted;
        this.isLoadedVideosNames = isLoadedVideosNames;
    }

    public EventMessage(ArrayList<String> videosNames, boolean isLoadedVideosNames) {
        this.videosNames = videosNames;
        this.isLoadedVideosNames = isLoadedVideosNames;
        this.cachingCompleted = false;
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
}
