package com.github.jbarr21.goproremote.data.api;

import com.github.jbarr21.goproremote.common.data.GoProState;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.functions.Func1;

public final class GoProUtils {

    public static final int HTTP_GONE = 410;

    private GoProUtils() { }

    /**
     * Fetches the GoPro camera state. First, we assume that the camera is on.
     * Then, if we get a GONE error response, we try to the camera off request.
     */
    public static Observable<byte[]> fetchCameraState(GoProApi goProApi) {
        return goProApi.fetchCameraStateWhileOn()
                .map(new CameraStateMapper(false))
                .onErrorResumeNext(throwable -> {
                    if (throwable instanceof HttpException) {
                        HttpException exception = (HttpException) throwable;
                        if (exception.code() == HTTP_GONE) {
                            return Observable.just(GoProState.CAMERA_OFF.rawState());
//                            return goProApi.fetchCameraStateWhileOff()
//                                    .map(new CameraStateMapper(false));
                        }
                    }
                    return Observable.error(throwable);
                });
    }

    static class CameraStateMapper implements Func1<Response<ResponseBody>, byte[]> {
        private boolean throwError = false;

        public CameraStateMapper(boolean throwError) {
            this.throwError = throwError;
        }

        @Override
        public byte[] call(Response<ResponseBody> response) {
            Throwable throwable = null;
            byte[] bytes = new byte[0];
            try {
                if (response != null && response.body() != null) {
                    bytes = response.body().bytes();
                }
            } catch (IOException e) {
                throwable = e;
            }
            if (throwable != null || bytes.length == 0) {
                if (throwError) {
                    throw new RuntimeException("Camera state is empty");
                } else {
                    bytes = GoProState.CAMERA_OFF.rawState();
                }
            }
            return bytes;
        }
    }
}
