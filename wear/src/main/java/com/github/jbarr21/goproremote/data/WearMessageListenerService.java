package com.github.jbarr21.goproremote.data;

import com.github.jbarr21.goproremote.GoProRemoteApp;
import com.github.jbarr21.goproremote.common.data.Constants;
import com.github.jbarr21.goproremote.common.data.GoProCommandResponse;
import com.github.jbarr21.goproremote.common.utils.RxEventBus;
import com.github.jbarr21.goproremote.ui.ProgressActivity.GoProCommandResponseEvent;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.squareup.moshi.Moshi;

import java.io.IOException;

import javax.inject.Inject;

import timber.log.Timber;

public class WearMessageListenerService extends WearableListenerService {

    @Inject Moshi moshi;
    @Inject RxEventBus bus;

    @Override
    public void onCreate() {
        super.onCreate();
        GoProRemoteApp.getComponent().inject(this);
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        Timber.d("Received message on path: %s", messageEvent.getPath());
        if (Constants.COMMAND_RESPONSE_PATH.equals(messageEvent.getPath())) {
            String data = new String(messageEvent.getData());
            try {
                GoProCommandResponse commandResponse = moshi.adapter(GoProCommandResponse.class).fromJson(data);
                bus.post(new GoProCommandResponseEvent(commandResponse));
            } catch (IOException e) {
                Timber.w(e, "Unable to parse gopro command response");
            }
        }
    }
}
