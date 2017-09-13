package com.example.sargiskh.accontechvideoapplication.services;

import android.app.IntentService;
import android.content.Intent;

import com.example.sargiskh.accontechvideoapplication.EventMessage;
import com.example.sargiskh.accontechvideoapplication.helpers.Constants;

import org.greenrobot.eventbus.EventBus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class VideosNamesDownloaderService extends IntentService {

    public VideosNamesDownloaderService() {
        super("VideosNamesDownloaderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        downloadVideosNames();
    }

    private void downloadVideosNames(){

        Document doc = null;
        try {
            doc = Jsoup.connect(Constants.BASE_URL).get();
        } catch (IOException e) {

        }

        Elements elements = doc.select("a");

        getVideosNamesList(elements);
    }

    private void getVideosNamesList(Elements elements) {

        ArrayList<String> names = new ArrayList<>();

        for (Element element : elements) {
            if (element.html().endsWith(".mp4")) {
                names.add(element.html());
            }
        }
        EventBus.getDefault().post(new EventMessage(names, true));
    }

}