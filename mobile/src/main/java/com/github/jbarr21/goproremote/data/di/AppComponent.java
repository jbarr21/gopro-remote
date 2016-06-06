package com.github.jbarr21.goproremote.data.di;

import com.github.jbarr21.goproremote.GoProRemoteApp;
import com.github.jbarr21.goproremote.common.data.di.AppModule;
import com.github.jbarr21.goproremote.common.data.di.DataModule;
import com.github.jbarr21.goproremote.data.service.MobileMessageListenerService;
import com.github.jbarr21.goproremote.data.service.GoProNotificationCmdReceiver;
import com.github.jbarr21.goproremote.ui.HomeActivity;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, DataModule.class})
public interface AppComponent {
    void inject(GoProRemoteApp app);
    void inject(HomeActivity activity);
    void inject(GoProNotificationCmdReceiver receiver);
    void inject(MobileMessageListenerService service);
}