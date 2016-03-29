package com.github.jbarr21.goproremote.util;

import com.github.jbarr21.goproremote.api.GoProApi;

import retrofit2.adapter.rxjava.HttpException;
import retrofit2.Response;
import rx.Observable;

public final class GoProUtils {

    public static final int HTTP_GONE = 410;

    private GoProUtils() { }

    /**
     * Fetches the GoPro camera state. First, we assume that the camera is on.
     * Then, if we get a GONE error response, we try to the camera off request.
     */
    public static Observable<byte[]> fetchCameraState(GoProApi goProApi) {
        return goProApi.fetchCameraStateWhileOn()
                .onErrorResumeNext(throwable -> {
                    if (throwable instanceof HttpException) {
                        HttpException exception = (HttpException) throwable;
                        if (exception.code() == HTTP_GONE) {
                            return goProApi.fetchCameraStateWhileOff();
                        }
                    }
                    return Observable.error(throwable);
                });
    }
}
