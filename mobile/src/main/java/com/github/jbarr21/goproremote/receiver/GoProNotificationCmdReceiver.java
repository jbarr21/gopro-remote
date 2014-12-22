package com.github.jbarr21.goproremote.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.github.jbarr21.goproremote.api.Apis;
import com.github.jbarr21.goproremote.api.GoProApi;
import com.github.jbarr21.goproremote.common.Constants;
import com.github.jbarr21.goproremote.util.GoProNotificationManager;

import timber.log.Timber;

public class GoProNotificationCmdReceiver extends BroadcastReceiver {

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
