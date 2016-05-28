package com.github.jbarr21.goproremote.common.utils;

import android.support.annotation.NonNull;

import rx.Observable;
import rx.Observable.Transformer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RxUtils {

    private static final Transformer<Object, Object> API_REQUEST_SCHEDULERS_TRANSFORMER = new Transformer<Object, Object>() {
        @Override
        public Observable<Object> call(Observable<Object> observable) {
            return observable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io());
        }
    };

    private RxUtils() { }

    @SuppressWarnings("unchecked")
    public static <T> Observable.Transformer<T, T> applyApiRequestSchedulers() {
        return (Observable.Transformer<T, T>) API_REQUEST_SCHEDULERS_TRANSFORMER;
    }

    public static void unsubscribeSafely(@NonNull Subscription... subscriptions) {
        for (Subscription subscription : subscriptions) {
            if (subscription != null && !subscription.isUnsubscribed()) {
                subscription.unsubscribe();
            }
        }
    }
}