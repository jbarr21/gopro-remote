package com.github.jbarr21.goproremote.data.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.github.jbarr21.goproremote.common.data.Constants;
import com.github.jbarr21.goproremote.common.data.GoProCommand;
import com.github.jbarr21.goproremote.common.data.GoProCommandRequest;
import com.github.jbarr21.goproremote.common.data.GoProCommandResponse;
import com.github.jbarr21.goproremote.common.data.GoProMode;
import com.github.jbarr21.goproremote.common.data.GoProState;
import com.github.jbarr21.goproremote.common.utils.GoProStateParser;
import com.github.jbarr21.goproremote.common.utils.MessageUtils;
import com.github.jbarr21.goproremote.common.utils.Parser;
import com.github.jbarr21.goproremote.data.api.Apis;
import com.github.jbarr21.goproremote.data.api.GoProApi;
import com.github.jbarr21.goproremote.data.api.GoProUtils;
import com.github.jbarr21.goproremote.data.network.WifiUtils;
import com.github.jbarr21.goproremote.ui.GoProNotificationManager;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class MobileMessageListenerService extends WearableListenerService {

    private GoogleApiClient googleApiClient;
    private GoProApi goProApi;
    private Moshi moshi;

    @Override
    public void onCreate() {
        super.onCreate();
        goProApi = Apis.getGoProApi();
        moshi = Parser.getMoshi();
        googleApiClient = new GoogleApiClient.Builder(this.getApplicationContext())
                .addApi(Wearable.API)
                .build();
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
                                    MessageUtils.sendGoProCommandResponse(googleApiClient, peerId, commandResponse);
                                }, throwable -> {
                                    String errorMessage = determineCause(throwable, command);
                                    GoProCommandResponse commandResponse = GoProCommandResponse.create(commandRequest.id(), MessageUtils.FAILURE, errorMessage);
                                    MessageUtils.sendGoProCommandResponse(googleApiClient, peerId, commandResponse);
                                });
                    }
                    break;
            }
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

    public static class GoProNotificationCmdReceiver extends BroadcastReceiver {

        public static final int EXTRA_TYPE_MODE = 20;
        public static final int EXTRA_TYPE_ACTION = 10;
        public static final int EXTRA_TYPE_LOG = 30;
        public static final int EXTRA_TYPE_DISMISS = 40;
        public static final int DEFAULT_NOTIFICAION = 50;

        public static final String TYPE = "type";
        public static final String EXTRA_MODE = "mode";
        public static final String EXTRA_ACTION = "action";
        public static final String EXTRA_LOG_MESSAGE = "log_message";

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final GoProApi goProApi = Apis.getGoProApi();
            final GoProNotificationManager notificaionManager = GoProNotificationManager.from(context);

            final int type = intent.getExtras().getInt(TYPE);
            switch (type) {
                case EXTRA_TYPE_LOG:
                    Timber.v(intent.getExtras().getString(EXTRA_LOG_MESSAGE));
                    break;
                case EXTRA_TYPE_MODE:
                    final int mode = intent.getExtras().getInt(EXTRA_MODE);
                    switch (mode) {
                        case Constants.SWITCH_TO_PHOTO:
                            goProApi.setPhotoMode().subscribe();
                            notificaionManager.showPhotoNotificaion();
                            break;
                        case Constants.SWITCH_TO_VIDEO:
                            goProApi.setVideoMode().subscribe();
                            notificaionManager.showVideoNotificaion();
                            break;
                        case DEFAULT_NOTIFICAION:
                            notificaionManager.showStartNotification();
                            break;
                        default:
                            Toast.makeText(context, "not supported", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case EXTRA_TYPE_ACTION:
                    final int action = intent.getExtras().getInt(EXTRA_ACTION);
                    switch (action) {
                        case Constants.TAKE_PHOTO:
                            goProApi.takePhoto().subscribe();
                            break;
                        case Constants.START_VIDEO:
                            goProApi.startRecording().subscribe();
                            break;
                        case Constants.STOP_VIDEO:
                            goProApi.stopRecording().subscribe();
                            break;
                        case Constants.POWER_ON:
                            goProApi.powerOn().subscribe();
                            break;
                        case Constants.POWER_OFF:
                            goProApi.powerOff().subscribe();
                            break;
                        default:
                            Toast.makeText(context, "not supported", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case EXTRA_TYPE_DISMISS:
                    //NotificationManagerCompat.from(context).cancelAll();
                    //notificaionManager.showStartNotification();
                    break;
            }
        }
    }
}
