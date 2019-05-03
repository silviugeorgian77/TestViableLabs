package com.timmystudios.webservicesutils.listeners;


public interface WriteToDiskListener {

    void onWriteToDiskProgressChanged(long writtenSize, long fileSize);
    String getDownloadIdentifier();

}
