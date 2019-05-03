package com.timmystudios.webservicesutils.services;



import com.timmystudios.webservicesutils.interceptors.DownloadProgressInterceptor;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface DownloadService {

    @Streaming
    @GET
    Call<ResponseBody> downloadFile(
            @Header(DownloadProgressInterceptor.DOWNLOAD_IDENTIFIER_HEADER) String downloadIdentifier,
            @Url String fileUrl
    );

}
