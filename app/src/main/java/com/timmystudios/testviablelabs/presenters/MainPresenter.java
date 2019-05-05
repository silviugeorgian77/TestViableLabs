package com.timmystudios.testviablelabs.presenters;

import com.timmystudios.testviablelabs.activities.MainActivity;
import com.timmystudios.testviablelabs.mappers.UserListResponseMapper;
import com.timmystudios.testviablelabs.models.User;
import com.timmystudios.testviablelabs.models.UserListResponse;
import com.timmystudios.testviablelabs.provider_services.UserProviderService;
import com.timmystudios.webservicesutils.WebServicesUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainPresenter {

    public enum Status {
        IDLE,
        FETCHING
    }

    private Status status = Status.IDLE;
    private MainActivity activity;
    private Map<String, String> userListParams = new HashMap<>();
    private List<User> userList = new ArrayList<>();
    private UserProviderService userProviderService;
    private final int RESULTS_PER_PAGE = 20;
    private int currentPage = 0;

    public MainPresenter(MainActivity activity) {
        this.activity = activity;
        userProviderService = WebServicesUtils.getService(UserProviderService.class);

        userListParams.put(UserProviderService.RESULTS, String.valueOf(RESULTS_PER_PAGE));
        increaseCurrentPage();
        fetchUserList();
    }

    public void increaseCurrentPage() {
        currentPage++;
        userListParams.put(UserProviderService.PAGE, String.valueOf(currentPage));
    }

    public void fetchUserList() {
        status = Status.FETCHING;
        activity.onUpdateUserListStarted();
        Call<UserListResponse> userListCall = userProviderService.getUsers(userListParams);
        userListCall.enqueue(new Callback<UserListResponse>() {
            @Override
            public void onResponse(Call<UserListResponse> call,
                                   Response<UserListResponse> response) {
                status = Status.IDLE;
                userList.addAll(UserListResponseMapper.mapUserList(response.body()));
                activity.onUpdateUserListSucceeded(userList);
            }

            @Override
            public void onFailure(Call<UserListResponse> call, Throwable t) {
                status = Status.IDLE;
                activity.onUpdateUserListFailed();
            }
        });
    }

    public Status getStatus() {
        return status;
    }
}
