package com.timmystudios.webservicesutils.interceptors;


import com.timmystudios.webservicesutils.listeners.DownloadProgressListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

public class DownloadProgressInterceptor implements Interceptor {

    private static List<DownloadProgressListener> downloadProgressListeners = new ArrayList<>();

    public static final String DOWNLOAD_IDENTIFIER_HEADER = "download_identifier";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());
        String downloadIdentifier = originalResponse.request().header(DOWNLOAD_IDENTIFIER_HEADER);
        ProgressResponseBody progressResponseBody = new ProgressResponseBody(
                downloadIdentifier,
                originalResponse.body()
        );
        return originalResponse.newBuilder()
                .body(progressResponseBody)
                .build();
    }

    public static void addProgressListener(
            DownloadProgressListener downloadProgressListener) {
        downloadProgressListeners.add(downloadProgressListener);
    }

    public static void removeProgressListener(
            DownloadProgressListener downloadProgressListener) {
        downloadProgressListeners.remove(downloadProgressListener);
    }

    public static void removeAllProgressListeners() {
        downloadProgressListeners.clear();
    }

    private static class ProgressResponseBody extends ResponseBody {

        private String downloadIdentifier;
        private ResponseBody responseBody;
        private BufferedSource bufferedSource;

        ProgressResponseBody(String downloadIdentifier,
                             ResponseBody responseBody) {
            this.downloadIdentifier = downloadIdentifier;
            this.responseBody = responseBody;
        }

        @Override public MediaType contentType() {
            return responseBody.contentType();
        }

        @Override public long contentLength() {
            return responseBody.contentLength();
        }

        @Override public BufferedSource source() {
            if (bufferedSource == null) {
                bufferedSource = Okio.buffer(getForwardingSource(responseBody.source()));
            }
            return bufferedSource;
        }

        private Source getForwardingSource(Source source) {
            return new ForwardingSource(source) {
                long totalBytesRead = 0L;

                @Override public long read(Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    // read() returns the number of bytes read, or -1 if this source is exhausted.
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                    String listenerDownloadIdentifier;
                    for (DownloadProgressListener listener : downloadProgressListeners) {
                        listenerDownloadIdentifier = listener.getDownloadIdentifier();
                        if (listenerDownloadIdentifier.equals(downloadIdentifier)) {
                            listener.onDownloadProgressChanged(
                                    totalBytesRead,
                                    responseBody.contentLength()
                            );
                        }
                    }
                    return bytesRead;
                }
            };
        }
    }

}
