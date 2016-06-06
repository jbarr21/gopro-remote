package com.github.jbarr21.goproremote.common.data.di;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.f2prateek.rx.preferences.RxSharedPreferences;
import com.github.jbarr21.goproremote.common.utils.RxEventBus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {
    private final Application app;

    public AppModule(Application app) {
        this.app = app;
    }

    @Provides
    @Singleton
    public Application provideApplication() {
        return app;
    }

    @Provides
    @Singleton
    public Context provideApplicationContext() {
        return app;
    }

//    @Provides
//    @Singleton
//    public FirebaseRemoteConfig provideFirebaseRemoteConfig() {
//        FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
//        remoteConfig.setDefaults(R.xml.remote_config_defaults);
//        return remoteConfig;
//    }
//
//    @Provides
//    @Singleton
//    public FirebaseAnalytics provideFirebaseAnalytics() {
//        return FirebaseAnalytics.getInstance(app);
//    }

    @Provides
    @Singleton
    public SharedPreferences provideSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(app);
    }

    @Provides
    @Singleton
    public RxSharedPreferences provideRxSharedPreferences(SharedPreferences prefs) {
        return RxSharedPreferences.create(prefs);
    }

    @Provides
    @Singleton
    public RxEventBus provideRxEventBus() {
        return new RxEventBus();
    }
}