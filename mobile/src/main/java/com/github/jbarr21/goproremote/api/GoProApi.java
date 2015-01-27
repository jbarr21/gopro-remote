package com.github.jbarr21.goproremote.api;

import retrofit.client.Response;
import retrofit.http.GET;
import rx.Observable;

public interface GoProApi {

    @GET("/bacpac/PW?p=%01")
    Observable<Response> powerOn();

    @GET("/bacpac/PW?p=%00")
    Observable<Response> powerOff();

    @GET("/camera/CM?p=%00")
    Observable<Response> setVideoMode();

    @GET("/camera/CM?p=%01")
    Observable<Response> setPhotoMode();

    @GET("/camera/CM?p=%02")
    Observable<Response> setBurstMode();

    @GET("/camera/CM?p=%03")
    Observable<Response> setTimelapseMode();

    @GET("/camera/SH?p=%01")
    Observable<Response> startRecording();

    @GET("/camera/SH?p=%00")
    Observable<Response> stopRecording();

    @GET("/camera/SH?p=%01")
    Observable<Response> takePhoto();

    @GET("/camera/se")
    Observable<Response> fetchCameraStateWhileOn();

    @GET("/bacpac/se")
    Observable<Response> fetchCameraStateWhileOff();
}
