package com.github.jbarr21.goproremote.service;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import timber.log.Timber;

public class GoProCommandRequestService extends WearableListenerService {
    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        Timber.d(messageEvent.getPath());
        if (false /* is proper message type */) {
            // do something
        }
    }
}
