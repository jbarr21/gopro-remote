package com.github.jbarr21.goproremote.data;

import android.app.Activity;
import android.os.Looper;

import com.github.jbarr21.goproremote.R;
import com.github.jbarr21.goproremote.common.data.GoProCommand;
import com.github.jbarr21.goproremote.common.data.GoProCommandRequest;
import com.github.jbarr21.goproremote.common.data.GoProMode;
import com.github.jbarr21.goproremote.common.utils.MessageUtils;
import com.github.jbarr21.goproremote.common.utils.RxEventBus;
import com.github.jbarr21.goproremote.ui.ProgressActivity;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.HashMap;

import javax.inject.Inject;

import timber.log.Timber;

public class RequestHandler {
    private static final HashMap<Integer, GoProCommand> COMMAND_MAP;
    static {
        COMMAND_MAP = new HashMap<>();
        COMMAND_MAP.put(R.id.powerOn, GoProCommand.POWER_ON);
        COMMAND_MAP.put(R.id.startRecording, GoProCommand.START_RECORDING);
        COMMAND_MAP.put(R.id.stopRecording, GoProCommand.STOP_RECORDING);
        COMMAND_MAP.put(R.id.powerOff, GoProCommand.POWER_OFF);
    }

    private MessageUtils messageUtils;
    private RxEventBus bus;
    private GoogleApiClient googleApiClient;

    @Inject
    public RequestHandler(MessageUtils messageUtils, RxEventBus bus, GoogleApiClient googleApiClient) {
        this.messageUtils = messageUtils;
        this.bus = bus;
        this.googleApiClient = googleApiClient;
    }

    public void onCommandClicked(final Activity activity, final GoProCommand command) {
        final GoProCommandRequest commandRequest = GoProCommandRequest.create(command, System.currentTimeMillis());
        messageUtils.sendGoProCommandMessage(googleApiClient, commandRequest)
                .subscribe(requestId -> {
                    Timber.d("sent message onNext - isOnMainThread? %b", Looper.myLooper() == Looper.getMainLooper());
                    Timber.d("Sent GoPro command (%s) successfully", command.name());
                    MessageUtils.disconnectGoogleApiClient(googleApiClient);
                    if (activity != null) {
                        ProgressActivity.showProgressOrFailure(activity, MessageUtils.SUCCESS, commandRequest.id());
                    }
                }, throwable -> {
                    Timber.d("sent message onError - isOnMainThread? %b", Looper.myLooper() == Looper.getMainLooper());
                    Timber.e(throwable, "Failed to send GoPro command: %s", command.name());
                    MessageUtils.disconnectGoogleApiClient(googleApiClient);
                    if (activity != null) {
                        ProgressActivity.showProgressOrFailure(activity, MessageUtils.FAILURE, commandRequest.id());
                    }
                });
    }

    public void onModeSelected(final Activity activity, final GoProMode mode) {
        Timber.d("selected mode %s", mode.name());
        final GoProCommand command = mode.getCommand();
        final GoProCommandRequest commandRequest = GoProCommandRequest.create(command, System.currentTimeMillis());
        messageUtils.sendGoProCommandMessage(googleApiClient, commandRequest)
                .subscribe(requestId -> {
                    Timber.d("sent message onNext - isOnMainThread? %b", Looper.myLooper() == Looper.getMainLooper());
                    Timber.d("Sent GoPro command (%s) successfully", command.name());
                    MessageUtils.disconnectGoogleApiClient(googleApiClient);
                    if (activity != null) {
                        ProgressActivity.showProgressOrFailure(activity, MessageUtils.SUCCESS, commandRequest.id());
                    }
                }, throwable -> {
                    Timber.d("sent message onError - isOnMainThread? %b", Looper.myLooper() == Looper.getMainLooper());
                    Timber.e(throwable, "Failed to send GoPro command: %s", command.name());
                    MessageUtils.disconnectGoogleApiClient(googleApiClient);
                    if (activity != null) {
                        ProgressActivity.showProgressOrFailure(activity, MessageUtils.FAILURE, commandRequest.id());
                    }
                });
    }
}
