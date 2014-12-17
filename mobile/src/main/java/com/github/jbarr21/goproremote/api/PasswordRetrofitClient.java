package com.github.jbarr21.goproremote.api;

import android.net.Uri;
import android.net.Uri.Builder;

import com.github.jbarr21.goproremote.data.ConfigStorage;

import java.io.IOException;
import java.util.Set;

import retrofit.client.Client;
import retrofit.client.Request;
import retrofit.client.Response;

public class PasswordRetrofitClient implements Client {

    private static final String PARAM_WIFI_PASSWORD = "t";

    private Client wrappedClient;
    private ConfigStorage configStorage;

    public PasswordRetrofitClient(Client wrappedClient, ConfigStorage configStorage) {
        this.wrappedClient = wrappedClient;
        this.configStorage = configStorage;
    }

    @Override
    public Response execute(Request request) throws IOException {
        Request newRequest = sign(request);
        return wrappedClient.execute(newRequest);
    }

    private Request sign(Request request) {
        Uri uri = Uri.parse(request.getUrl());
        Uri.Builder uriBuilder = new Builder()
                .scheme(uri.getScheme())
                .authority(uri.getAuthority())
                .path(uri.getPath())
                .appendQueryParameter(PARAM_WIFI_PASSWORD, configStorage.getWifiPassword());

        Set<String> queryParamNames = uri.getQueryParameterNames();
        for (String queryParamName : queryParamNames) {
            uriBuilder.appendQueryParameter(queryParamName, uri.getQueryParameter(queryParamName));
        }

        return new Request(request.getMethod(), uriBuilder.toString(), request.getHeaders(), request.getBody());
    }
}
