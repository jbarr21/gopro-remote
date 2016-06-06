package com.github.jbarr21.goproremote.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;

import com.github.jbarr21.goproremote.GoProRemoteApp;
import com.github.jbarr21.goproremote.common.data.GoProCommand;
import com.github.jbarr21.goproremote.common.data.GoProCommandRequest;
import com.github.jbarr21.goproremote.common.utils.MessageUtils;
import com.github.jbarr21.goproremote.ui.ProgressActivity;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import javax.inject.Inject;

import timber.log.Timber;

public class WearNotificationReceiver extends BroadcastReceiver {

    public static final String ACTION_SEND_COMMAND = "send_command";
    public static final String ACTION_LOG_MESSAGE = "log_message";
    public static final String ACTION_DISMISS = "dismiss";

    public static final String EXTRA_GO_PRO_COMMAND = "go_pro_command";
    public static final String EXTRA_LOG_MESSAGE = "log_message";

    @Inject MessageUtils messageUtils;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        GoProRemoteApp.getComponent().inject(this);
        Timber.v("WearNotificationReceiver.onReceive() - %s", intent);
        String action = intent.getAction();
        if (!TextUtils.isEmpty(action)) {
            switch (action) {
                case ACTION_SEND_COMMAND:
                    final GoProCommand command = (GoProCommand) intent.getExtras().getSerializable(EXTRA_GO_PRO_COMMAND);
                    final GoProCommandRequest commandRequest = GoProCommandRequest.create(command, System.currentTimeMillis());
                    final GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                            .addApi(Wearable.API)
                            .build();

                    Timber.v("[Send Command] %s", command.name());
                    messageUtils.sendGoProCommandMessage(googleApiClient, commandRequest)
                            .subscribe(integer -> {
                                Timber.d("sent message onNext - isOnMainThread? %b", Looper.myLooper() == Looper.getMainLooper());
                                Timber.d("Sent GoPro command (%s) successfully", command.name());
                                MessageUtils.disconnectGoogleApiClient(googleApiClient);
                                ProgressActivity.showProgressOrFailure(context, MessageUtils.SUCCESS, commandRequest.id());
                            }, throwable -> {
                                Timber.d("sent message onError - isOnMainThread? %b", Looper.myLooper() == Looper.getMainLooper());
                                Timber.e(throwable, "Failed to send GoPro command: %s", command.name());
                                MessageUtils.disconnectGoogleApiClient(googleApiClient);
                                ProgressActivity.showProgressOrFailure(context, MessageUtils.FAILURE, commandRequest.id());
                            });
                    break;
                case ACTION_LOG_MESSAGE:
                    Timber.v("[Log] %s", intent.getExtras().getString(EXTRA_LOG_MESSAGE));
                    break;
                case ACTION_DISMISS:
                    NotificationManagerCompat.from(context).cancelAll();
                    break;
            }
        }
    }
}
