package com.github.jbarr21.goproremote.util;

import android.app.Notification;
import android.app.Notification.Action;
import android.app.Notification.WearableExtender;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import com.github.jbarr21.goproremote.R;
import com.github.jbarr21.goproremote.activity.ControlActivity;
import com.github.jbarr21.goproremote.activity.ModeActivity;
import com.github.jbarr21.goproremote.activity.PagerActivity;
import com.github.jbarr21.goproremote.common.GoProCommand;
import com.github.jbarr21.goproremote.receiver.WearNotificationReceiver;

import timber.log.Timber;

public class WearNotificationManager {

    private static final int NOTIFICATION_ID_STATUS = 0;
    private static final int NOTIFICATION_ID_STREAM = 1;

    private static WearNotificationManager instance;

    private final Context appContext;
    private final NotificationManager notificationManager;
    private Notification.Builder streamNotification;
    private Notification.Builder statusNotification;
    private CharSequence title, text;

    private WearNotificationManager(@NonNull final Context context) {
        Timber.d("Creating new wearable notification manager");
        appContext = context.getApplicationContext();
        notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
        statusNotification = newStatusNotification("", "");
    }

    public static WearNotificationManager from(@NonNull final Context context) {
        if (instance == null) {
            instance = new WearNotificationManager(context);
        }
        return instance;
    }

    public void showStreamNotification(CharSequence title, CharSequence text) {
        streamNotification = newStreamNotification(title, text);
        notificationManager.notify(NOTIFICATION_ID_STREAM, streamNotification.build());
    }

    public void hideStreamNotification() {
        notificationManager.cancel(NOTIFICATION_ID_STREAM);
    }

    public void showStatusNotification(CharSequence title, CharSequence text) {
        statusNotification = newStatusNotification(title, text);
        notificationManager.notify(NOTIFICATION_ID_STATUS, statusNotification.build());
    }

    public void updateStatusNotification(CharSequence title, CharSequence text) {
        saveNotificationLabels(title, text);
        statusNotification.setContentTitle(title)
                .setContentText(text);
        notificationManager.notify(NOTIFICATION_ID_STATUS, statusNotification.build());
    }

    private Notification.Builder newStreamNotification(CharSequence title, CharSequence text) {
        return new Notification.Builder(appContext)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLocalOnly(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setVibrate(new long[]{0, 50}) // Vibrate to bring card to top of stream
                .setDeleteIntent(newLogPendingIntent("Delete intent triggered", PendingIntent.FLAG_CANCEL_CURRENT))
                .addAction(new Action(R.drawable.ic_fullscreen_white_48dp, "Fullscreen", newLaunchFullscreenPendingIntent()))
                .addAction(new Action(R.drawable.ic_network_wifi_white_48dp, "Connect to Wi-Fi", newConnectWifiPendingIntent()));
    }

    private Notification.Builder newStatusNotification(CharSequence contentTitle, CharSequence contentText) {
        saveNotificationLabels(title, text);
        return new Notification.Builder(appContext)
                .setContentTitle(contentTitle)  // Current status and quick action icon
                .setContentText(contentText)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLocalOnly(true)
                .setPriority(Notification.PRIORITY_MAX)
                .setVibrate(new long[]{0, 50}) // Vibrate to bring card to top of stream
                .setDeleteIntent(newLogPendingIntent("Delete intent triggered", PendingIntent.FLAG_CANCEL_CURRENT))
                .extend(new Notification.WearableExtender()
                                .setBackground(BitmapFactory.decodeResource(appContext.getResources(), R.drawable.background))
                                .addPage(new Notification.Builder(appContext)
                                                .extend(new WearableExtender()
                                                                .setDisplayIntent(newControlPendingIntent()) // On, Off, Start, Stop
                                                                .setCustomSizePreset(WearableExtender.SIZE_FULL_SCREEN)
                                                )
                                                .build()
                                )
                                .addPage(new Notification.Builder(appContext)
                                                .extend(new WearableExtender()
                                                                .setDisplayIntent(newModePendingIntent()) // Mode wearable list view
                                                                .setCustomSizePreset(WearableExtender.SIZE_FULL_SCREEN)
                                                )
                                                .build()
                                )
                                .addAction(new Action(R.drawable.ic_network_wifi_white_48dp, "Connect to Wi-Fi", newConnectWifiPendingIntent()))
                );
    }

    private PendingIntent newLaunchFullscreenPendingIntent() {
        Intent intent = new Intent(appContext, PagerActivity.class);
        return PendingIntent.getActivity(appContext, 0, intent, 0); // TODO: launch new task?
    }

    private PendingIntent newControlPendingIntent() {
        Intent displayIntent = new Intent(appContext, ControlActivity.class);
        return PendingIntent.getActivity(appContext, 0, displayIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent newModePendingIntent() {
        Intent displayIntent = new Intent(appContext, ModeActivity.class);
        return PendingIntent.getActivity(appContext, 0, displayIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent newConnectWifiPendingIntent() {
        return PendingIntent.getBroadcast(appContext, 0, newConnectWifiIntent(appContext), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static Intent newConnectWifiIntent(Context appContext) {
        return new Intent(appContext, WearNotificationReceiver.class)
                .setAction(WearNotificationReceiver.ACTION_SEND_COMMAND)
                .putExtra(WearNotificationReceiver.EXTRA_GO_PRO_COMMAND, GoProCommand.CONNECT_WIFI);
    }

    private PendingIntent newLogPendingIntent(final String logMessage, int flags) {
        final Intent intent = new Intent(appContext, WearNotificationReceiver.class)
                .setAction(WearNotificationReceiver.ACTION_LOG_MESSAGE)
                .putExtra(WearNotificationReceiver.EXTRA_LOG_MESSAGE, logMessage);
        return PendingIntent.getBroadcast(appContext, (int) (Math.random() * 99999), intent, flags);
    }

    private void saveNotificationLabels(CharSequence title, CharSequence text) {
        this.title = title;
        this.text = text;
    }
}
