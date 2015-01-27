package com.github.jbarr21.goproremote.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.wearable.activity.ConfirmationActivity;
import android.text.TextUtils;

import com.github.jbarr21.goproremote.activity.ProgressActivity;

public class NavUtils {

    private static final int REQUEST_CODE_PROGRESS = 0;
    private static final int REQUEST_CODE_RESULT = 1;

    private NavUtils() { }

    public static void showProgressOrFailure(Context context, boolean success, long commandRequestId) {
        if (success) {
            showProgressAnimation(context, commandRequestId);
        } else {
            showResultAnimation(context, false, "Not connected to mobile");
        }
    }

    private static void showProgressAnimation(Context context, long commandRequestId) {
        launchIntent(context, REQUEST_CODE_PROGRESS, new Intent(context, ProgressActivity.class)
                .putExtra(ProgressActivity.EXTRA_COMMAND_REQUEST_ID, commandRequestId));
    }

    public static void showResultAnimation(Context context, boolean success, CharSequence message) {
        if (TextUtils.isEmpty(message)) {
            if (success) {
                message = "Command completed";
            } else {
                message = "Command failed";
            }
        }

        launchIntent(context, REQUEST_CODE_RESULT, new Intent(context, ConfirmationActivity.class)
                .putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, success ? ConfirmationActivity.SUCCESS_ANIMATION : ConfirmationActivity.FAILURE_ANIMATION)
                .putExtra(ConfirmationActivity.EXTRA_MESSAGE, message));
    }

    private static void launchIntent(Context context, int requestCode, Intent intent) {
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(intent, requestCode);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
