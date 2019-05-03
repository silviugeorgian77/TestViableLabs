package com.timmystudios.webservicesutils.listeners;


public interface DownloadProgressListener {

    void onDownloadProgressChanged(long bytesRead, long contentLength);
    String getDownloadIdentifier();

}
