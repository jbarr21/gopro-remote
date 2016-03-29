package com.github.jbarr21.goproremote.api;

import android.text.TextUtils;

import com.github.jbarr21.goproremote.data.ConfigStorage;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class PasswordSigningInterceptor implements Interceptor {

    private static final String PARAM_WIFI_PASSWORD = "t";
    private static final String PARAM_OPTIONS = "p";

    private ConfigStorage configStorage;

    public PasswordSigningInterceptor(ConfigStorage configStorage) {
        this.configStorage = configStorage;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        HttpUrl httpUrl = request.url();

        Set<String> paramNames = httpUrl.queryParameterNames();
        Map<String, String> params = new LinkedHashMap<>();
        for (String name : paramNames) {
            params.put(name, httpUrl.queryParameter(name));
        }

        HttpUrl.Builder signedUrl = request.url().newBuilder();

        for (String name : paramNames) {
            signedUrl.removeAllQueryParameters(name);
        }

        // WiFi password must be the first param
        signedUrl.addQueryParameter(PARAM_WIFI_PASSWORD, configStorage.getWifiPassword());

        for (String name : paramNames) {
            signedUrl.addQueryParameter(name, params.get(name));
        }

        Request signedRequest = request.newBuilder().url(signedUrl.build()).build();
        return chain.proceed(signedRequest);
    }
}