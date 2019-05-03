package com.timmystudios.testviablelabs;

import android.app.Application;

import com.timmystudios.webservicesutils.WebServicesUtils;

public class TestViableLabsApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        WebServicesUtils.init(this);
    }
}
