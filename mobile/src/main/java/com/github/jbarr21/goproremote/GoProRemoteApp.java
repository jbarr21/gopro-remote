package com.github.jbarr21.goproremote;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.github.jbarr21.goproremote.common.data.di.AppModule;
import com.github.jbarr21.goproremote.common.data.di.DataModule;
import com.github.jbarr21.goproremote.data.di.AppComponent;
import com.github.jbarr21.goproremote.data.di.DaggerAppComponent;

import timber.log.Timber;
import timber.log.Timber.DebugTree;

public class GoProRemoteApp extends Application {
    private static GoProRemoteApp instance;
    private static AppComponent appComponent;

    public static GoProRemoteApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        if (BuildConfig.DEBUG) {
            Timber.plant(new DebugTree());
        }
        Stetho.initializeWithDefaults(this);
        setupDagger();
    }

    void setupDagger() {
        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .dataModule(new DataModule(BuildConfig.DEBUG))
                .build();
        appComponent.inject(this);
    }

    public static AppComponent getComponent() {
        return appComponent;
    }
}
