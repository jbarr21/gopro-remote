package com.github.jbarr21.goproremote.common;

import android.support.annotation.NonNull;

import rx.Subscription;

public class RxUtils {

    private RxUtils() { }

    public static void unsubscribeSafely(@NonNull Subscription... subscriptions) {
        for (Subscription subscription : subscriptions) {
            if (subscription != null && !subscription.isUnsubscribed()) {
                subscription.unsubscribe();
            }
        }
    }
}