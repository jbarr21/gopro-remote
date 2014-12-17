package com.github.jbarr21.goproremote.api;

import com.github.jbarr21.goproremote.GoProRemoteApp;
import com.github.jbarr21.goproremote.data.ConfigStorage;
import com.google.gson.Gson;
import com.squareup.okhttp.ConnectionSpec;
import com.squareup.okhttp.OkHttpClient;

import java.net.CookieHandler;
import java.util.Arrays;

import retrofit.RestAdapter;
import retrofit.RestAdapter.LogLevel;
import retrofit.client.Client;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

public final class Apis {

    private static final String GO_PRO_ENDPOINT_URL = "http://10.5.5.9/";

    private static GoProApi goProApi;
    private static OkHttpClient okHttpClient;

    private Apis() { }

    public static GoProApi getGoProApi() {
        if (goProApi == null) {
            RestAdapter restAdapter = getRestAdapterBuilder(GO_PRO_ENDPOINT_URL).build();
            goProApi = restAdapter.create(GoProApi.class);
        }
        return goProApi;
    }

    private static RestAdapter.Builder getRestAdapterBuilder(final String endpointUrl) {
        return new RestAdapter.Builder()
                .setLogLevel(LogLevel.FULL)
                .setConverter(new GsonConverter(new Gson()))
                .setClient(getRetrofitClient())
                .setEndpoint(endpointUrl);
    }

    private static Client getRetrofitClient() {
        Client client = new OkClient(getOkHttpClient());
        ConfigStorage configStorage = new ConfigStorage(GoProRemoteApp.getInstance());
        return new PasswordRetrofitClient(client, configStorage);
    }

    public static OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient();
            okHttpClient.setConnectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT));
            okHttpClient.setCookieHandler(CookieHandler.getDefault());
        }
        return okHttpClient;
    }
}
