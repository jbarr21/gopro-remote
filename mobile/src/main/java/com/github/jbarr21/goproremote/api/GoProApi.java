package com.github.jbarr21.goproremote.api;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.GET;

public interface GoProApi {

    @GET("/bacpac/PW?t=p=%01")
    void powerOn(Callback<Response> callback);

    @GET("/bacpac/PW?p=%00")
    void powerOff(Callback<Response> callback);

    @GET("/camera/CM?p=%00")
    void setVideoMode(Callback<Response> callback);

    @GET("/camera/CM?p=%01")
    void setPhotoMode(Callback<Response> callback);

    @GET("/camera/SH?p=%01")
    void startVideo(Callback<Response> callback);

    @GET("/camera/SH?p=%00")
    void stopVideo(Callback<Response> callback);

    @GET("/camera/SH?p=%01")
    void takePhoto(Callback<Response> callback);
}
