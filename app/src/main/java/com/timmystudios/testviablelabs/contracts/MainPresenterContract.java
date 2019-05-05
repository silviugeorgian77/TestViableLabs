package com.timmystudios.testviablelabs.contracts;

public interface MainPresenterContract {
    enum Status {
        IDLE,
        FETCHING
    }
    void init(MainActivityContract mainActivityContract);
    void increaseCurrentPage();
    void fetchUserList();
    Status getStatus();
}
