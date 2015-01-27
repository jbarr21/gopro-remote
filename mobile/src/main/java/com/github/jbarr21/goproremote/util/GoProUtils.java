package com.github.jbarr21.goproremote.util;

import com.github.jbarr21.goproremote.api.GoProApi;

import org.apache.http.HttpStatus;

import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;

public final class GoProUtils {

    private GoProUtils() { }

    /**
     * Fetches the GoPro camera state. First, we assume that the camera is on.
     * Then, if we get a GONE error response, we try to the camera off request.
     */
    public static Observable<Response> fetchCameraState(GoProApi goProApi) {
        return goProApi.fetchCameraStateWhileOn()
                .onErrorResumeNext(throwable -> {
                    if (throwable instanceof RetrofitError) {
                        RetrofitError re = (RetrofitError) throwable;
                        Response resp = re.getResponse();
                        if (resp != null && resp.getStatus() == HttpStatus.SC_GONE) {
                            return goProApi.fetchCameraStateWhileOff();
                        }
                    }
                    return Observable.error(throwable);
                });
    }
}
