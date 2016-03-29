package com.github.jbarr21.goproremote.service;

import android.net.Uri;

import com.github.jbarr21.goproremote.api.Apis;
import com.github.jbarr21.goproremote.api.GoProApi;
import com.github.jbarr21.goproremote.common.Constants;
import com.github.jbarr21.goproremote.common.GoProCommand;
import com.github.jbarr21.goproremote.common.GoProCommandRequest;
import com.github.jbarr21.goproremote.common.GoProCommandResponse;
import com.github.jbarr21.goproremote.common.GoProMode;
import com.github.jbarr21.goproremote.common.GoProState;
import com.github.jbarr21.goproremote.common.MessageUtils;
import com.github.jbarr21.goproremote.util.GoProUtils;
import com.github.jbarr21.goproremote.util.WifiUtils;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.gson.Gson;
import com.twotoasters.servos.util.StreamUtils;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import retrofit2.adapter.rxjava.HttpException;
import retrofit2.Response;
import rx.Observable;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class MobileMessageListenerService extends WearableListenerService {

    private GoogleApiClient googleApiClient;
    private GoProApi goProApi;
    private Gson gson;

    @Override
    public void onCreate() {
        super.onCreate();
        goProApi = Apis.getGoProApi();
        gson = new Gson();
        googleApiClient = new GoogleApiClient.Builder(this.getApplicationContext())
                .addApi(Wearable.API)
                .build();
        googleApiClient.connect();
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
            String data = new String(messageEvent.getData());
            final GoProCommandRequest commandRequest = gson.fromJson(data, GoProCommandRequest.class);
            final String peerId = messageEvent.getSourceNodeId();
            final GoProCommand command = commandRequest.getCommand();
            Timber.d("onMessageReceived - cmd: %s", command.name());

            switch (command) {
                case CONNECT_WIFI:
                    WifiUtils.addGoProWifiNetwork(this).subscribe();
                    break;
                case GET_STATE:
                    GoProUtils.fetchCameraState(goProApi)
                            .subscribeOn(Schedulers.io())
                            .subscribe(response -> Timber.d("success"), throwable -> Timber.e(throwable, "failure"));
                    break;
                default:
                    Observable<Object> commandObservable = createGoProCommandObservable(command);
                    if (commandObservable != null) {
                        commandObservable
                                .delay(command == GoProCommand.POWER_ON || command == GoProCommand.POWER_OFF ? 5 : 1, TimeUnit.SECONDS)
                                .flatMap(response -> GoProUtils.fetchCameraState(goProApi))
                                .map(this::cameraStateFromResponse)
                                .flatMap(cameraState -> confirmCameraState(command, cameraState))
                                .subscribeOn(Schedulers.io())
                                .subscribe(success -> {
                                    String message = success ? command.getSuccessMessage() : command.getFailureMessage();
                                    GoProCommandResponse commandResponse = new GoProCommandResponse(commandRequest.getId(), success, message);
                                    MessageUtils.sendGoProCommandResponse(googleApiClient, peerId, commandResponse);
                                }, throwable -> {
                                    String errorMessage = determineCause(throwable, command);
                                    GoProCommandResponse commandResponse = new GoProCommandResponse(commandRequest.getId(), MessageUtils.FAILURE, errorMessage);
                                    MessageUtils.sendGoProCommandResponse(googleApiClient, peerId, commandResponse);
                                });
                    }
                    break;
            }
        }
    }

    private Observable<Object> createGoProCommandObservable(GoProCommand command) {
        switch (command) {
            case POWER_ON:          return goProApi.powerOn();
            case POWER_OFF:         return goProApi.powerOff();
            case SET_VIDEO:         return goProApi.setVideoMode();
            case SET_PHOTO:         return goProApi.setPhotoMode();
            case SET_BURST:         return goProApi.setBurstMode();
            case SET_TIMELAPSE:     return goProApi.setTimelapseMode();
            case START_RECORDING:   return goProApi.startRecording();
            case STOP_RECORDING:    return goProApi.stopRecording();
            case TAKE_PHOTO:        return goProApi.takePhoto();
            default:                return null;
        }
    }

    private GoProState cameraStateFromResponse(byte[] response) {
        return GoProState.from(response);
    }

    private Observable<Boolean> confirmCameraState(GoProCommand command, GoProState cameraState) {
        return Observable.just(isGoProCommandSuccessful(command, cameraState));
    }

    private boolean isGoProCommandSuccessful(GoProCommand command, GoProState cameraState) {
        switch (command) {
            case POWER_ON:          return cameraState.isPowerOn();
            case POWER_OFF:         return !cameraState.isPowerOn();
            case SET_VIDEO:         return cameraState.getCurrentMode() == GoProMode.VIDEO;
            case SET_PHOTO:         return cameraState.getCurrentMode() == GoProMode.PHOTO;
            case SET_BURST:         return cameraState.getCurrentMode() == GoProMode.BURST;
            case SET_TIMELAPSE:     return cameraState.getCurrentMode() == GoProMode.TIMELAPSE;
            case START_RECORDING:   return cameraState.isRecording();
            case STOP_RECORDING:    return !cameraState.isRecording();
            case TAKE_PHOTO:        return true; // TODO: figure out how to test
            default:                return false;
        }
    }

    private String determineCause(Throwable throwable, GoProCommand command) {
        if (!WifiUtils.isConnectedToGoProWifi(this)) {
            return "Disconnected from GoPro";
        } else if (throwable instanceof SocketTimeoutException) {
            return "Command timed out";
        } else if (throwable instanceof HttpException) {
            HttpException exception = (HttpException) throwable;
            if (exception.code() == GoProUtils.HTTP_GONE) {
                return "GoPro is powered off";
            } else {
                return command.getFailureMessage();
            }
        }
        return null;
    }

    public class GoProCommandException extends Exception { }
}
