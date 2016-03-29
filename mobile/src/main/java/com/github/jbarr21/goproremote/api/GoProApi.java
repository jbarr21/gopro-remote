package com.github.jbarr21.goproremote.api;

import retrofit2.http.GET;
import rx.Observable;

public interface GoProApi {

    @GET("bacpac/PW?p=%01")
    Observable<Object> powerOn();

    @GET("bacpac/PW?p=%00")
    Observable<Object> powerOff();

    @GET("camera/CM?p=%00")
    Observable<Object> setVideoMode();

    @GET("camera/CM?p=%01")
    Observable<Object> setPhotoMode();

    @GET("camera/CM?p=%02")
    Observable<Object> setBurstMode();

    @GET("camera/CM?p=%03")
    Observable<Object> setTimelapseMode();

    @GET("camera/SH?p=%01")
    Observable<Object> startRecording();

    @GET("camera/SH?p=%00")
    Observable<Object> stopRecording();

    @GET("camera/SH?p=%01")
    Observable<Object> takePhoto();

    @GET("camera/se")
    Observable<byte[]> fetchCameraStateWhileOn();

    @GET("bacpac/se")
    Observable<byte[]> fetchCameraStateWhileOff();
}
