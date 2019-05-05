package com.timmystudios.testviablelabs.activities;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.timmystudios.testviablelabs.R;
import com.timmystudios.testviablelabs.adapters.UserAdapter;
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

public class MainActivity extends AppCompatActivity {

    private List<User> userList = new ArrayList<>();
    private UserProviderService userProviderService;
    private Map<String, String> userListParams = new HashMap<>();
    private ContentLoadingProgressBar progressBar;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private final int RESULTS_PER_PAGE = 20;
    private int currentPage = 0;
    private int previousItemCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progress_bar);
        recyclerView = findViewById(R.id.recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
        );
        recyclerView.setLayoutManager(layoutManager);
        userAdapter = new UserAdapter(userList);
        recyclerView.setAdapter(userAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1)) {
                    increaseCurrentPage();
                    updateUserList();
                }
            }
        });

        userProviderService = WebServicesUtils.getService(UserProviderService.class);

        userListParams.put(UserProviderService.RESULTS, String.valueOf(RESULTS_PER_PAGE));
        increaseCurrentPage();
        updateUserList();
    }

    private void increaseCurrentPage() {
        currentPage++;
        userListParams.put(UserProviderService.PAGE, String.valueOf(currentPage));
    }

    private void updateUserList() {
        if (userList.isEmpty()) {
            progressBar.show();
        }
        Call<UserListResponse> userListCall = userProviderService.getUsers(userListParams);
        userListCall.enqueue(new Callback<UserListResponse>() {
            @Override
            public void onResponse(Call<UserListResponse> call,
                                   Response<UserListResponse> response) {
                previousItemCount = userList.size();
                userList.addAll(UserListResponseMapper.mapUserList(response.body()));
                showUserList();
                progressBar.hide();
            }

            @Override
            public void onFailure(Call<UserListResponse> call, Throwable t) {
                showErrorDialog();
                progressBar.hide();
            }
        });
    }

    private void showUserList() {
        userAdapter.setUserList(userList);
        int addedItemsCount = userList.size() - previousItemCount;
        userAdapter.notifyItemRangeInserted(previousItemCount, addedItemsCount);
    }

    private void showErrorDialog() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.error_title)
            .setMessage(R.string.error_description)
            .setPositiveButton(R.string.error_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Map<String, String> userListParams = new HashMap<>();
                    userListParams.put(UserProviderService.PAGE, String.valueOf(1));
                    userListParams.put(UserProviderService.RESULTS, String.valueOf(RESULTS_PER_PAGE));
                    updateUserList();
                }
            })
            .show();
    }
}
