package com.github.jbarr21.goproremote.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.github.jbarr21.goproremote.R;
import com.github.jbarr21.goproremote.common.data.GoProCommandResponse;
import com.github.jbarr21.goproremote.common.utils.RxEventBus;
import com.github.jbarr21.goproremote.common.utils.RxUtils;

import java.util.concurrent.TimeoutException;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class ProgressActivity extends GoProActivity {

    public static final String EXTRA_COMMAND_REQUEST_ID = "request_id";

    private static final int REQUEST_CODE_PROGRESS = 0;
    private static final int REQUEST_CODE_RESULT = 1;

    private long requestId;
    private Subscription sub;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        requestId = getIntent().getLongExtra(EXTRA_COMMAND_REQUEST_ID, 0);

        sub = RxEventBus.events(GoProCommandResponseEvent.class)
                .filter(new Func1<GoProCommandResponseEvent, Boolean>() {
                    @Override
                    public Boolean call(GoProCommandResponseEvent event) {
                        return event != null && event.response != null && requestId == event.response.requestId();
                    }
                })
                //.timeout(10, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<GoProCommandResponseEvent>() {
                    @Override
                    public void call(GoProCommandResponseEvent event) {
                        showResultAnimation(ProgressActivity.this, event.response.success(), event.response.message());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (throwable instanceof TimeoutException) {
                            showResultAnimation(ProgressActivity.this, false, "Operation timed out");
                        } else {
                            String errMsg = "Unable to show progress result animation";
                            Toast.makeText(ProgressActivity.this, errMsg, Toast.LENGTH_SHORT).show();
                            Timber.e(throwable, "Unable to show progress result animation");
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxUtils.unsubscribeSafely(sub);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }

    public static class GoProCommandResponseEvent {
        public GoProCommandResponse response;
        public GoProCommandResponseEvent(GoProCommandResponse response) {
            this.response = response;
        }
    }
}
