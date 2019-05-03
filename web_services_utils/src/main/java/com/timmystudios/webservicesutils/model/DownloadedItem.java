package com.timmystudios.webservicesutils.model;


import java.io.File;

public class DownloadedItem {

    private File file;
    private String url;

    private static DownloadedItem invalidDownloadedItem;

    public DownloadedItem(File file, String url) {
        this.file = file;
        this.url = url;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public static DownloadedItem getInvalidDownloadedItem() {
        if (invalidDownloadedItem == null) {
            invalidDownloadedItem = new DownloadedItem(null, null);
        }
        return invalidDownloadedItem;
    }

    public boolean isValid() {
        return file != null && url != null && !url.equals("");
    }
}
