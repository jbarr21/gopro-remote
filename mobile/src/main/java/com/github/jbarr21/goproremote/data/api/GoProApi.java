package com.github.jbarr21.goproremote.data.api;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import rx.Observable;

public interface GoProApi {

    @GET("bacpac/PW?p=%01")
    Observable<Response<ResponseBody>> powerOn();

    @GET("bacpac/PW?p=%00")
    Observable<Response<ResponseBody>> powerOff();

    @GET("camera/CM?p=%00")
    Observable<Response<ResponseBody>> setVideoMode();

    @GET("camera/CM?p=%01")
    Observable<Response<ResponseBody>> setPhotoMode();

    @GET("camera/CM?p=%02")
    Observable<Response<ResponseBody>> setBurstMode();

    @GET("camera/CM?p=%03")
    Observable<Response<ResponseBody>> setTimelapseMode();

    @GET("camera/SH?p=%01")
    Observable<Response<ResponseBody>> startRecording();

    @GET("camera/SH?p=%00")
    Observable<Response<ResponseBody>> stopRecording();

    @GET("camera/SH?p=%01")
    Observable<Response<ResponseBody>> takePhoto();

    @GET("camera/se")
    Observable<Response<ResponseBody>> fetchCameraStateWhileOn();

    @GET("bacpac/se")
    Observable<Response<ResponseBody>> fetchCameraStateWhileOff();
}
