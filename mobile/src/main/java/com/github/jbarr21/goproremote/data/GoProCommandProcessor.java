package com.github.jbarr21.goproremote.data;

import android.app.Application;

import com.github.jbarr21.goproremote.common.data.GoProCommand;
import com.github.jbarr21.goproremote.common.data.GoProCommandRequest;
import com.github.jbarr21.goproremote.common.data.GoProCommandResponse;
import com.github.jbarr21.goproremote.common.data.GoProMode;
import com.github.jbarr21.goproremote.common.data.GoProState;
import com.github.jbarr21.goproremote.common.data.api.GoProApi;
import com.github.jbarr21.goproremote.common.data.api.GoProUtils;
import com.github.jbarr21.goproremote.common.utils.GoProStateParser;
import com.github.jbarr21.goproremote.common.utils.MessageUtils;
import com.github.jbarr21.goproremote.common.utils.WifiUtils;
import com.google.android.gms.common.api.GoogleApiClient;
import com.squareup.moshi.Moshi;

import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.schedulers.Schedulers;

public class GoProCommandProcessor {

    Application app;
    GoogleApiClient googleApiClient;
    GoProApi goProApi;
    Moshi moshi;
    MessageUtils messageUtils;

    @Inject
    public GoProCommandProcessor(GoogleApiClient googleApiClient, GoProApi goProApi, Moshi moshi, MessageUtils messageUtils) {
        this.googleApiClient = googleApiClient;
        this.goProApi = goProApi;
        this.moshi = moshi;
        this.messageUtils = messageUtils;
    }

    public void process(GoProCommand command, GoProCommandRequest commandRequest, String peerId) {
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
                        GoProCommandResponse commandResponse = GoProCommandResponse.create(commandRequest.id(), success, message);
                        messageUtils.sendGoProCommandResponse(googleApiClient, peerId, commandResponse);
                    }, throwable -> {
                        String errorMessage = determineCause(throwable, command);
                        GoProCommandResponse commandResponse = GoProCommandResponse.create(commandRequest.id(), MessageUtils.FAILURE, errorMessage);
                        messageUtils.sendGoProCommandResponse(googleApiClient, peerId, commandResponse);
                    });
        }
    }

    private Observable createGoProCommandObservable(GoProCommand command) {
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
        return GoProStateParser.from(response);
    }

    private Observable<Boolean> confirmCameraState(GoProCommand command, GoProState cameraState) {
        return Observable.just(isGoProCommandSuccessful(command, cameraState));
    }

    private boolean isGoProCommandSuccessful(GoProCommand command, GoProState cameraState) {
        switch (command) {
            case POWER_ON:          return cameraState.isPowerOn();
            case POWER_OFF:         return !cameraState.isPowerOn();
            case SET_VIDEO:         return cameraState.currentMode() == GoProMode.VIDEO;
            case SET_PHOTO:         return cameraState.currentMode() == GoProMode.PHOTO;
            case SET_BURST:         return cameraState.currentMode() == GoProMode.BURST;
            case SET_TIMELAPSE:     return cameraState.currentMode() == GoProMode.TIMELAPSE;
            case START_RECORDING:   return cameraState.recording();
            case STOP_RECORDING:    return !cameraState.recording();
            case TAKE_PHOTO:        return true; // TODO: figure out how to test
            default:                return false;
        }
    }

    private String determineCause(Throwable throwable, GoProCommand command) {
        if (!WifiUtils.isConnectedToGoProWifi(app)) {
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
}
