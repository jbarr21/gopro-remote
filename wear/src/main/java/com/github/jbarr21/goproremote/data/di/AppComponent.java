package com.github.jbarr21.goproremote.data.di;

import com.github.jbarr21.goproremote.GoProRemoteApp;
import com.github.jbarr21.goproremote.common.data.di.AppModule;
import com.github.jbarr21.goproremote.common.data.di.DataModule;
import com.github.jbarr21.goproremote.data.WearMessageListenerService;
import com.github.jbarr21.goproremote.data.WearNotificationReceiver;
import com.github.jbarr21.goproremote.ui.ProgressActivity;
import com.github.jbarr21.goproremote.ui.main.DrawerActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, DataModule.class})
public interface AppComponent {
    void inject(GoProRemoteApp app);
    void inject(DrawerActivity activity);
    void inject(ProgressActivity activity);
    void inject(WearNotificationReceiver receiver);
    void inject(WearMessageListenerService service);
}