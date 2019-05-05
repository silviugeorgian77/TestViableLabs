package com.timmystudios.testviablelabs.contracts;

import com.timmystudios.testviablelabs.models.User;

import java.util.List;

public interface MainActivityContract {
    void onUpdateUserListStarted();
    void onUpdateUserListSucceeded(List<User> userList);
    void onUpdateUserListFailed();
}
