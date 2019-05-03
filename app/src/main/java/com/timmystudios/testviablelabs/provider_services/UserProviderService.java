package com.timmystudios.testviablelabs.provider_services;

import com.timmystudios.testviablelabs.models.UserListResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface UserProviderService {

    String PAGE = "page";
    String RESULTS = "results";

    @GET("https://randomuser.me/api")
    Call<UserListResponse> getUsers(@QueryMap Map<String, String> params);

}
