package xyz.bnayagrawal.android.kat.net;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by bnayagrawal on 8/3/18.
 */

public final class Url {
    public static final String BASE_URL= "https://katcr.date";
    public static final String MY_GIT_ACCOUNT_URL = "https://github.com/bnayagrawal";

    public static final String PATH_SEARCH = "search";
    public static final String PATH_MOVIES = "Movies";
    public static final String PATH_TV = "Tv";
    public static final String PATH_MUSIC = "Music";
    public static final String PATH_BOOKS = "Books";
    public static final String PATH_GAMES = "Games";
    public static final String PATH_APPS = "Applications";

    public static class Builder {

        public static String buildMagnetLink(@NonNull String[] trackerList,@NonNull String hash) {
            String magnetUrl = "magnet:?xt=urn:";
            String infoHash = "btih:" + hash + "&dn=";
            String trackers = "";
            try {
                for (String tracker : trackerList)
                    trackers = trackers.concat("&tr=" + URLEncoder.encode(tracker, "utf-8"));
                return magnetUrl + infoHash + trackers;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
