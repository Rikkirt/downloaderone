package com.sapher.youtubedl;

public interface DownloadProgressCallback {

    void onProgressUpdate(String destination, float progress, long etaInSeconds);

}
