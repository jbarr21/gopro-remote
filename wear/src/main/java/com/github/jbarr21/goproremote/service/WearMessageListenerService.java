package com.github.jbarr21.goproremote.service;

import com.github.jbarr21.goproremote.activity.ProgressActivity.GoProCommandResponseEvent;
import com.github.jbarr21.goproremote.common.Constants;
import com.github.jbarr21.goproremote.common.GoProCommandResponse;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.gson.Gson;
import com.twotoasters.servos.util.otto.BusProvider;

import timber.log.Timber;

public class WearMessageListenerService extends WearableListenerService {

    private Gson gson;

    @Override
    public void onCreate() {
        super.onCreate();
        gson = new Gson();
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        Timber.d("Received message on path: %s", messageEvent.getPath());
        if (Constants.COMMAND_RESPONSE_PATH.equals(messageEvent.getPath())) {
            String data = new String(messageEvent.getData());
            GoProCommandResponse commandResponse = gson.fromJson(data, GoProCommandResponse.class);
            BusProvider.post(new GoProCommandResponseEvent(commandResponse));
        }
    }
}
