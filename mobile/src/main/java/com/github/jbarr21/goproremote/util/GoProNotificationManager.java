package com.github.jbarr21.goproremote.util;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.github.jbarr21.goproremote.R;
import com.github.jbarr21.goproremote.common.Constants;
import com.github.jbarr21.goproremote.receiver.GoProNotificationCmdReceiver;

public class GoProNotificationManager {

    private static final String GROUP_ID = "goProGroup";

    private static final int NOTIFY_ID_ZERO = 0;
    private static final int NOTIFY_ID_ONE = 1;
    private static final int NOTIFY_ID_TWO = 2;

    private static GoProNotificationManager instance;

    private final Context appContext;
    private final NotificationManagerCompat mNotificationManager;

    private GoProNotificationManager(final Context context) {
        appContext = context.getApplicationContext();
        mNotificationManager = NotificationManagerCompat.from(context);
    }

    public static GoProNotificationManager from(final Context context) {
        if (instance == null) {
            instance = new GoProNotificationManager(context);
        }
        return instance;
    }

    public void showStartNotification() {
        // visible on the phone
        Notification summary = new NotificationCompat.Builder(appContext)
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .setContentTitle(appContext.getString(R.string.notification_title_gopro_device))
                .setContentText(appContext.getString(R.string.notification_content_gopro_device))
                .setGroup(GROUP_ID)
                .setGroupSummary(true)
                .build();
        mNotificationManager.notify(NOTIFY_ID_ZERO, summary);

        // first notification on wear
        Notification mode = new NotificationCompat.Builder(appContext)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(appContext.getString(R.string.notification_title_gopro_wear_remote))
                .setContentText(appContext.getString(
                        R.string.notification_content_gopro_wear))
                .addAction(android.R.drawable.ic_menu_camera, appContext.getString(R.string.gopro_action_mode_photo), getSwitchModePendingIntent(Constants.SWITCH_TO_PHOTO))
                .addAction(android.R.drawable.ic_menu_slideshow, appContext.getString(R.string.gopro_action_mode_video), getSwitchModePendingIntent(Constants.SWITCH_TO_VIDEO))
                .setGroup(GROUP_ID)
                .setSortKey("1")
                .build();

        mNotificationManager.notify(NOTIFY_ID_ONE, mode);

        Notification n1 = new NotificationCompat.Builder(appContext)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(appContext.getString(R.string.notification_title_more))
                .setContentText(appContext.getString(R.string.notification_content_more))
                .addAction(android.R.drawable.ic_lock_power_off, appContext.getString(R.string.gopro_action_on), getActionPendingIntent(Constants.POWER_ON))
                .addAction(android.R.drawable.ic_media_pause, appContext.getString(
                        R.string.gopro_action_off), getActionPendingIntent(Constants.POWER_OFF))
                .setDeleteIntent(getActionDismissedPendingIntent())
                .setGroup(GROUP_ID)
                .setSortKey("2")
                .build();
        mNotificationManager.notify(NOTIFY_ID_TWO, n1);
    }

    public void hideStartNotification() {
        mNotificationManager.cancel(NOTIFY_ID_ZERO);
    }

    public void showPhotoNotificaion() {
        mNotificationManager.cancel(NOTIFY_ID_TWO);

        Notification n1 = new NotificationCompat.Builder(appContext)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(appContext.getString(R.string.notification_title_photo))
                .setContentText(appContext.getString(R.string.notification_content_photo))
                .addAction(android.R.drawable.ic_menu_camera, appContext.getString(R.string.gopro_action_take_photo), getActionPendingIntent(Constants.TAKE_PHOTO))
                .addAction(android.R.drawable.ic_menu_revert, appContext.getString(
                        R.string.notification_back), getShowDefaultNotificationPendingIntent())
                .setDeleteIntent(getActionDismissedPendingIntent())
                .setGroup(GROUP_ID)
                .setSortKey("1")
                .build();

        mNotificationManager.notify(NOTIFY_ID_ONE, n1);
    }

    public void showVideoNotificaion() {
        mNotificationManager.cancel(NOTIFY_ID_TWO);

        Notification n1 = new NotificationCompat.Builder(appContext)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(appContext.getString(R.string.notification_title_video))
                .setContentText(appContext.getString(
                        R.string.notification_title_content_video))
                .addAction(android.R.drawable.ic_media_play, appContext.getString(R.string.gopro_action_start_video), getActionPendingIntent(Constants.START_VIDEO))
                .addAction(android.R.drawable.ic_media_pause, appContext.getString(
                                R.string.gopro_action_stop_video), getActionPendingIntent(Constants.STOP_VIDEO))
                .addAction(android.R.drawable.ic_menu_revert, appContext.getString(
                                R.string.notification_back), getShowDefaultNotificationPendingIntent())
                .setDeleteIntent(getActionDismissedPendingIntent())
                .setGroup(GROUP_ID)
                .setSortKey("1")
                .build();
        mNotificationManager.notify(NOTIFY_ID_ONE, n1);
    }

    private PendingIntent getActionDismissedPendingIntent() {
        final Intent intent = new Intent(appContext, GoProNotificationCmdReceiver.class);
        intent.putExtra(GoProNotificationCmdReceiver.TYPE,
                GoProNotificationCmdReceiver.EXTRA_TYPE_DISMISS);
        return PendingIntent.getBroadcast(appContext, (int) (Math.random() * 99999), intent, 0);
    }

    private PendingIntent getActionPendingIntent(final int action) {
        final Intent intent = new Intent(appContext, GoProNotificationCmdReceiver.class);
        intent.putExtra(GoProNotificationCmdReceiver.TYPE,
                GoProNotificationCmdReceiver.EXTRA_TYPE_ACTION);
        intent.putExtra(GoProNotificationCmdReceiver.EXTRA_ACTION, action);
        return PendingIntent.getBroadcast(appContext, (int) (Math.random() * 99999), intent, 0);
    }

    private PendingIntent getShowDefaultNotificationPendingIntent() {
        final Intent intent = new Intent(appContext, GoProNotificationCmdReceiver.class);
        intent.putExtra(GoProNotificationCmdReceiver.TYPE,
                GoProNotificationCmdReceiver.EXTRA_TYPE_MODE);
        intent.putExtra(GoProNotificationCmdReceiver.EXTRA_MODE,
                GoProNotificationCmdReceiver.DEFAULT_NOTIFICAION);
        return PendingIntent.getBroadcast(appContext, (int) (Math.random() * 99999), intent, 0);
    }

    private PendingIntent getSwitchModePendingIntent(final int switchToPhoto) {
        final Intent intent = new Intent(appContext, GoProNotificationCmdReceiver.class);
        intent.putExtra(GoProNotificationCmdReceiver.TYPE,
                GoProNotificationCmdReceiver.EXTRA_TYPE_MODE);
        intent.putExtra(GoProNotificationCmdReceiver.EXTRA_MODE, switchToPhoto);
        return PendingIntent.getBroadcast(appContext, (int) (Math.random() * 99999), intent, 0);
    }

    private PendingIntent getLogPendingIntent(final String logMessage) {
        final Intent intent = new Intent(appContext, GoProNotificationCmdReceiver.class);
        intent.putExtra(GoProNotificationCmdReceiver.EXTRA_LOG_MESSAGE, logMessage);
        return PendingIntent.getBroadcast(appContext, (int) (Math.random() * 99999), intent, 0);
    }
}
