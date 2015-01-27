package com.github.jbarr21.goproremote.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;

import com.github.jbarr21.goproremote.common.GoProCommand;
import com.github.jbarr21.goproremote.common.GoProCommandRequest;
import com.github.jbarr21.goproremote.common.MessageUtils;
import com.github.jbarr21.goproremote.util.NavUtils;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import timber.log.Timber;

public class WearNotificationReceiver extends BroadcastReceiver {

    public static final String ACTION_SEND_COMMAND = "send_command";
    public static final String ACTION_LOG_MESSAGE = "log_message";
    public static final String ACTION_DISMISS = "dismiss";

    public static final String EXTRA_GO_PRO_COMMAND = "go_pro_command";
    public static final String EXTRA_LOG_MESSAGE = "log_message";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Timber.v("WearNotificationReceiver.onReceive() - %s", intent);
        String action = intent.getAction();
        if (!TextUtils.isEmpty(action)) {
            switch (action) {
                case ACTION_SEND_COMMAND:
                    GoProCommand command = (GoProCommand) intent.getExtras().getSerializable(EXTRA_GO_PRO_COMMAND);
                    GoProCommandRequest commandRequest = new GoProCommandRequest(command);
                    Timber.v("[Send Command] %s", command.name());
                    GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                            .addApi(Wearable.API)
                            .build();

                    MessageUtils.sendGoProCommandMessage(googleApiClient, commandRequest)
                            .subscribe(requestId -> {
                                Timber.d("sent message onNext - isOnMainThread? %b", Looper.myLooper() == Looper.getMainLooper());
                                Timber.d("Sent GoPro command (%s) successfully", command.name());
                                MessageUtils.disconnectGoogleApiClient(googleApiClient);
                                NavUtils.showProgressOrFailure(context, MessageUtils.SUCCESS, commandRequest.getId());
                            }, throwable -> {
                                Timber.d("sent message onError - isOnMainThread? %b", Looper.myLooper() == Looper.getMainLooper());
                                Timber.e(throwable, "Failed to send GoPro command: %s", command.name());
                                MessageUtils.disconnectGoogleApiClient(googleApiClient);
                                NavUtils.showProgressOrFailure(context, MessageUtils.FAILURE, commandRequest.getId());
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
