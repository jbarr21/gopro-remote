package com.github.jbarr21.goproremote.data.service;

import android.net.Uri;

import com.github.jbarr21.goproremote.GoProRemoteApp;
import com.github.jbarr21.goproremote.common.data.Constants;
import com.github.jbarr21.goproremote.common.data.GoProCommand;
import com.github.jbarr21.goproremote.common.data.GoProCommandRequest;
import com.github.jbarr21.goproremote.common.data.api.GoProApi;
import com.github.jbarr21.goproremote.common.data.api.GoProUtils;
import com.github.jbarr21.goproremote.common.utils.WifiUtils;
import com.github.jbarr21.goproremote.data.GoProCommandProcessor;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.squareup.moshi.Moshi;

import java.io.IOException;

import javax.inject.Inject;

import rx.schedulers.Schedulers;
import timber.log.Timber;

public class MobileMessageListenerService extends WearableListenerService {
    @Inject GoogleApiClient googleApiClient;
    @Inject GoProApi goProApi;
    @Inject Moshi moshi;
    @Inject GoProCommandProcessor commandProcessor;

    @Override
    public void onCreate() {
        super.onCreate();
        GoProRemoteApp.getComponent().inject(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        Uri uri = Uri.parse(messageEvent.getPath());
        Timber.d("onMessageReceived - path: %s", uri.toString());
        if (Constants.COMMAND_REQUEST_PATH.equals(messageEvent.getPath())) {
            GoProCommandRequest commandRequest;
            GoProCommand tempCommand;
            String data = new String(messageEvent.getData());
            try {
                commandRequest = moshi.adapter(GoProCommandRequest.class).fromJson(data);
                tempCommand = commandRequest.command();
            } catch (IOException e) {
                Timber.w(e, "Unable to parse gopro command request");
                return;
            }

            final GoProCommand command = tempCommand;
            final String peerId = messageEvent.getSourceNodeId();
            Timber.d("onMessageReceived - cmd: %s", command.name());

            switch (command) {
                case CONNECT_WIFI:
                    WifiUtils.addGoProWifiNetwork(this)
                            .subscribe(
                                    it -> Timber.d("Successfully connected to WiFi"),
                                    e -> Timber.e(e, "Error connecting to WiFi"));
                    break;
                case GET_STATE:
                    GoProUtils.fetchCameraState(goProApi)
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    response -> Timber.d("success"),
                                    throwable -> Timber.e(throwable, "failure"));
                    break;
                default:
                    commandProcessor.process(command, commandRequest, peerId);
                    break;
            }
        }
    }




}
