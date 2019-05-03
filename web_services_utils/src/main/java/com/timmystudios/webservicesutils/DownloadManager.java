package com.timmystudios.webservicesutils;


import com.timmystudios.webservicesutils.listeners.FetchCompletedListener;
import com.timmystudios.webservicesutils.listeners.WriteToDiskListener;
import com.timmystudios.webservicesutils.model.DownloadedItem;
import com.timmystudios.webservicesutils.services.DownloadService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

class DownloadManager {

    private String downloadIdentifier;
    private String url;
    private String pathToDirectory;
    private String fileName;
    private Call<ResponseBody> call;
    private static final List<DownloadManager> downloadsInProgress = new ArrayList<>();
    private static final List<WriteToDiskListener> writeToDiskListeners = new ArrayList<>();
    private static final List<FetchCompletedListener> fetchCompletedListeners = new ArrayList<>();

    private static final String ERROR_DOWNLOAD_FAILED = "Could not download file.";
    private static final String ERROR_WRITE_TO_DISK_FAILED = "Could not save file.";

    protected static Single<DownloadedItem> downloadFile(String downloadIdentifier,
                                                         String url,
                                                         String pathToDirectory,
                                                         String fileName) {
        DownloadManager downloadManager = new DownloadManager(
                downloadIdentifier,
                url,
                pathToDirectory,
                fileName
        );
        return downloadManager.beginDownloadFile();
    }

    protected static void cancelDownloadFile(String downloadIdentifier) {
        synchronized (downloadsInProgress) {
            Iterator<DownloadManager> downloadManagerIterator = downloadsInProgress.iterator();
            DownloadManager downloadManager;
            while (downloadManagerIterator.hasNext()) {
                downloadManager = downloadManagerIterator.next();
                if (downloadIdentifier.equals(downloadManager.downloadIdentifier)) {
                    downloadManager.call.cancel();
                    downloadManagerIterator.remove();
                }
            }
        }
    }

    private DownloadManager(String downloadIdentifier,
                            String url,
                            String pathToDirectory,
                            String fileName) {
        this.downloadIdentifier = downloadIdentifier;
        this.url = url;
        this.pathToDirectory = pathToDirectory;
        this.fileName = fileName;
    }

    private Single<DownloadedItem> beginDownloadFile() {
        synchronized (downloadsInProgress) {
            downloadsInProgress.add(this);
        }
        return Single.create(new SingleOnSubscribe<DownloadedItem>() {
            @Override
            public void subscribe(final SingleEmitter<DownloadedItem> e) throws Exception {
                DownloadService downloadService
                        = WebServicesUtils.getService(DownloadService.class);
                call = downloadService.downloadFile(downloadIdentifier, url);

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call,
                                           retrofit2.Response<ResponseBody> response) {
                        String currentDownloadIdentifier;
                        for (FetchCompletedListener fetchCompletedListener :
                                fetchCompletedListeners) {
                            currentDownloadIdentifier
                                    = fetchCompletedListener.getDownloadIdentifier();
                            if (currentDownloadIdentifier.equals(downloadIdentifier)) {
                                fetchCompletedListener.onFetchCompleted(
                                        call,
                                        response
                                );
                                break;
                            }
                        }

                        // Init The Throwable Objects
                        Throwable failedDownloadThrowable
                                = new Throwable(ERROR_DOWNLOAD_FAILED);
                        Throwable failedSaveToDiskThrowable
                                = new Throwable(ERROR_WRITE_TO_DISK_FAILED);
                        // End Init The Throwable Objects

                        ResponseBody responseBody = response.body();
                        if (response.isSuccessful() && responseBody != null) {
                            long fileSize = responseBody.contentLength();
                            InputStream inputStream = responseBody.byteStream();
                            File file = writeResponseBodyToDisk(fileSize, inputStream);
                            if (pathToDirectory != null && file != null) {
                                DownloadedItem downloadedItem = new DownloadedItem(
                                        file,
                                        url
                                );
                                synchronized (downloadsInProgress) {
                                    downloadsInProgress.remove(DownloadManager.this);
                                }
                                e.onSuccess(downloadedItem);
                            } else {
                                synchronized (downloadsInProgress) {
                                    downloadsInProgress.remove(DownloadManager.this);
                                }
                                e.onSuccess(new DownloadedItem(null, ""));
                                onFailure(call, failedSaveToDiskThrowable);
                            }
                        } else {
                            downloadsInProgress.remove(DownloadManager.this);
                            e.onSuccess(new DownloadedItem(null, ""));
                            onFailure(call, failedDownloadThrowable);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        synchronized (downloadsInProgress) {
                            downloadsInProgress.remove(DownloadManager.this);
                        }
                        e.onError(t);
                    }
                });
            }
        });
    }

    /**
     * Returns the written on disk {@link File}, or null if the write failed.
     * @return
     */
    private File writeResponseBodyToDisk(long fileSize, InputStream inputStream) {
        try {
            File directory = new File(pathToDirectory);
            if (!directory.exists()) {
                directory.mkdir();
            }

            String fullPath = pathToDirectory + "/" + fileName;
            File file = new File(fullPath);

            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long writtenSize = 0;

                outputStream = new FileOutputStream(file);

                int read;
                String listenerDownloadIdentifier;
                while (true) {
                    read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    writtenSize += read;

                    for (WriteToDiskListener writeToDiskListener: writeToDiskListeners) {
                        listenerDownloadIdentifier = writeToDiskListener.getDownloadIdentifier();
                        if (listenerDownloadIdentifier.equals(downloadIdentifier)) {
                            writeToDiskListener.onWriteToDiskProgressChanged(writtenSize, fileSize);
                        }
                    }
                }

                outputStream.flush();

                return file;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void addWriteToDiskListener(WriteToDiskListener writeToDiskListener) {
        writeToDiskListeners.add(writeToDiskListener);
    }

    public static void removeWriteToDiskListener(WriteToDiskListener writeToDiskListener) {
        writeToDiskListeners.remove(writeToDiskListener);
    }

    public static void clearWriteToDiskListeners() {
        writeToDiskListeners.clear();
    }

    public static void addFetchCompletedListener(
            FetchCompletedListener fetchCompletedListener) {
        fetchCompletedListeners.add(fetchCompletedListener);
    }

    public static void removeFetchCompletedListener(
            FetchCompletedListener fetchCompletedListener) {
        fetchCompletedListeners.remove(fetchCompletedListener);
    }

    public static void clearFetchCompletedListener() {
        fetchCompletedListeners.clear();
    }
}
