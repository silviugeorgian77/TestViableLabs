package com.timmystudios.webservicesutils.listeners;

import okhttp3.ResponseBody;
import retrofit2.Call;

public interface FetchCompletedListener {

    void onFetchCompleted(Call<ResponseBody> call,
                          retrofit2.Response<ResponseBody> response);
    String getDownloadIdentifier();

}
