package com.github.jbarr21.goproremote.util;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RxAndroidUtils {

    private static final Observable.Transformer<Object, Object> API_REQUEST_SCHEDULERS_TRANSFORMER
            = observable -> observable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io());

    private static final Observable.Transformer<Object, Object> DATABASE_READ_SCHEDULERS_TRANSFORMER
            = observable -> observable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());

    private RxAndroidUtils() { }

    @SuppressWarnings("unchecked")
    public static <T> Observable.Transformer<T, T> applyApiRequestSchedulers() {
        return (Observable.Transformer<T, T>) API_REQUEST_SCHEDULERS_TRANSFORMER;
    }

    @SuppressWarnings("unchecked")
    public static <T> Observable.Transformer<T, T> applyDatabaseReadSchedulers() {
        return (Observable.Transformer<T, T>) DATABASE_READ_SCHEDULERS_TRANSFORMER;
    }
}