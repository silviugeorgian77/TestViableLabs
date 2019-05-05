package com.timmystudios.testviablelabs.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.timmystudios.testviablelabs.R;
import com.timmystudios.testviablelabs.adapters.UserAdapter;
import com.timmystudios.testviablelabs.contracts.MainActivityContract;
import com.timmystudios.testviablelabs.contracts.MainPresenterContract;
import com.timmystudios.testviablelabs.models.User;
import com.timmystudios.testviablelabs.presenters.MainPresenter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MainActivityContract {

    private List<User> userList = new ArrayList<>();
    private MainPresenterContract mainPresenterContract;
    private ContentLoadingProgressBar progressBar;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
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
        userAdapter = new UserAdapter();
        recyclerView.setAdapter(userAdapter);

        mainPresenterContract = new MainPresenter();
        mainPresenterContract.init(this);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1)
                        && mainPresenterContract.getStatus() == MainPresenter.Status.IDLE) {
                    mainPresenterContract.increaseCurrentPage();
                    mainPresenterContract.fetchUserList();
                }
            }
        });
    }

    @Override
    public void onUpdateUserListStarted() {
        previousItemCount = userList.size();
        if (userList.isEmpty()) {
            progressBar.show();
        }
    }

    @Override
    public void onUpdateUserListSucceeded(List<User> userList) {
        this.userList = userList;
        userAdapter.setUserList(userList);
        int addedItemsCount = userList.size() - previousItemCount;
        userAdapter.notifyItemRangeInserted(previousItemCount, addedItemsCount);
        progressBar.hide();
    }

    @Override
    public void onUpdateUserListFailed() {
        showErrorDialog();
        progressBar.hide();
    }

    private void showErrorDialog() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.error_title)
            .setMessage(R.string.error_description)
            .setPositiveButton(R.string.error_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mainPresenterContract.fetchUserList();
                }
            })
            .show();
    }
}
