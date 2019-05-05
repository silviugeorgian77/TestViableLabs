package com.timmystudios.testviablelabs.presenters;

import com.timmystudios.testviablelabs.contracts.MainActivityContract;
import com.timmystudios.testviablelabs.contracts.MainPresenterContract;
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

public class MainPresenter implements MainPresenterContract {

    private Status status = Status.IDLE;
    private MainActivityContract mainActivityContract;
    private Map<String, String> userListParams = new HashMap<>();
    private List<User> userList = new ArrayList<>();
    private UserProviderService userProviderService;
    private final int RESULTS_PER_PAGE = 20;
    private int currentPage = 0;

    @Override
    public void init(MainActivityContract mainActivityContract) {
        this.mainActivityContract = mainActivityContract;
        userProviderService = WebServicesUtils.getService(UserProviderService.class);

        userListParams.put(UserProviderService.RESULTS, String.valueOf(RESULTS_PER_PAGE));
        increaseCurrentPage();
        fetchUserList();
    }

    @Override
    public void increaseCurrentPage() {
        currentPage++;
        userListParams.put(UserProviderService.PAGE, String.valueOf(currentPage));
    }

    @Override
    public void fetchUserList() {
        status = Status.FETCHING;
        mainActivityContract.onUpdateUserListStarted();
        Call<UserListResponse> userListCall = userProviderService.getUsers(userListParams);
        userListCall.enqueue(new Callback<UserListResponse>() {
            @Override
            public void onResponse(Call<UserListResponse> call,
                                   Response<UserListResponse> response) {
                status = Status.IDLE;
                userList.addAll(UserListResponseMapper.mapUserList(response.body()));
                mainActivityContract.onUpdateUserListSucceeded(userList);
            }

            @Override
            public void onFailure(Call<UserListResponse> call, Throwable t) {
                status = Status.IDLE;
                mainActivityContract.onUpdateUserListFailed();
            }
        });
    }

    @Override
    public Status getStatus() {
        return status;
    }
}
