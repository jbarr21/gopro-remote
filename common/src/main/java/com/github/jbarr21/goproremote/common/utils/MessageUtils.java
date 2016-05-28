package com.github.jbarr21.goproremote.common.utils;

import android.os.Looper;
import android.support.annotation.NonNull;

import com.github.jbarr21.goproremote.common.data.Constants;
import com.github.jbarr21.goproremote.common.data.GoProCommandRequest;
import com.github.jbarr21.goproremote.common.data.GoProCommandResponse;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.squareup.moshi.Moshi;
import com.twotoasters.servos.util.ListUtils;

import java.net.ConnectException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static com.github.jbarr21.goproremote.common.data.Constants.GO_PRO_COMMAND_TIMEOUT_SEC;
import static com.github.jbarr21.goproremote.common.data.Constants.WEARABLE_CONN_TIMEOUT_MILLIS;

public class MessageUtils {

    public static final boolean SUCCESS = true;
    public static final boolean FAILURE = false;

    private static final Moshi MOSHI = Parser.getMoshi();

    private MessageUtils() { }

    public static Observable<Integer> sendGoProCommandMessage(@NonNull final GoogleApiClient googleApiClient, final GoProCommandRequest commandRequest) {
        return connectGoogleApiClient(googleApiClient)
                .flatMap(new Func1<ConnectionResult, Observable<String>>() {
                    @Override
                    public Observable<String> call(ConnectionResult connectionResult) {
                        return fetchPeerId(googleApiClient);
                    }
                })
                .flatMap(new Func1<String, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(String peerId) {
                        return sendCommandRequest(googleApiClient, peerId, commandRequest);
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    private static Observable<ConnectionResult> connectGoogleApiClient(final GoogleApiClient googleApiClient) {
        return Observable.defer(new Func0<Observable<ConnectionResult>>() {
            @Override
            public Observable<ConnectionResult> call() {
                ConnectionResult connectionResult = googleApiClient.blockingConnect(WEARABLE_CONN_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
                if (connectionResult != null && connectionResult.isSuccess()) {
                    return Observable.just(connectionResult);
                } else {
                    return Observable.error(new ConnectException(connectionResult != null ? connectionResult.toString() : "Failed to connect to Google API client"));
                }
            }
        });
    }

    private static Observable<String> fetchPeerId(GoogleApiClient googleApiClient) {
        Timber.d("fetchPeerId - isOnMainThread? %b", Looper.myLooper() == Looper.getMainLooper());
        List<Node> nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await(WEARABLE_CONN_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS).getNodes();
        return !ListUtils.isEmpty(nodes)
            ? Observable.just(nodes.get(0).getId())
            : Observable.<String>error(new IllegalStateException("Unable to fetch peer id"));
    }

    private static Observable<Integer> sendCommandRequest(GoogleApiClient googleApiClient, String peerId, GoProCommandRequest commandRequest) {
        ensureGoogleApiClientConnected(googleApiClient);
        Timber.d("connectGoogleApiClient - sendCommandRequest? %b", Looper.myLooper() == Looper.getMainLooper());
        byte[] data = MOSHI.adapter(GoProCommandRequest.class).toJson(commandRequest).getBytes();
        int requestId = Wearable.MessageApi
                .sendMessage(googleApiClient, peerId, Constants.COMMAND_REQUEST_PATH, data)
                .await(GO_PRO_COMMAND_TIMEOUT_SEC, TimeUnit.SECONDS)
                .getRequestId();

        boolean requestSent = requestId != MessageApi.UNKNOWN_REQUEST_ID;
        Timber.d("GoPro command request sent = %b, apiClientConnected = %b", requestSent, googleApiClient.isConnected());
        return requestSent
                ? Observable.just(requestId)
                : Observable.<Integer>error(new Exception("Unable to send command message"));
    }

    public static Observable<Integer> sendGoProCommandResponse(GoogleApiClient googleApiClient, String peerId, GoProCommandResponse response) {
        ensureGoogleApiClientConnected(googleApiClient);
        Timber.d("connectGoogleApiClient - sendCommandRequest? %b", Looper.myLooper() == Looper.getMainLooper());
        byte[] data = MOSHI.adapter(GoProCommandResponse.class).toJson(response).getBytes();
        int responseRequestId = Wearable.MessageApi
                .sendMessage(googleApiClient, peerId, Constants.COMMAND_RESPONSE_PATH, data)
                .await(GO_PRO_COMMAND_TIMEOUT_SEC, TimeUnit.SECONDS)
                .getRequestId();

        boolean responseSent = responseRequestId != MessageApi.UNKNOWN_REQUEST_ID;
        Timber.d("GoPro command response sent = %b, apiClientConnected = %b", responseSent, googleApiClient.isConnected());
        return responseSent
                ? Observable.just(responseRequestId)
                : Observable.<Integer>error(new Exception("Unable to send command message"));
    }

    public static void ensureGoogleApiClientConnected(GoogleApiClient googleApiClient) {
        if (googleApiClient != null && !googleApiClient.isConnected()) {
            googleApiClient.blockingConnect(1, TimeUnit.SECONDS);
        }
    }

    public static void disconnectGoogleApiClient(GoogleApiClient googleApiClient) {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }
}
