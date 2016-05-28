package com.github.jbarr21.goproremote.common.data;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

@AutoValue
public abstract class GoProCommandResponse {
    public abstract long requestId();
    public abstract boolean success();
    @Nullable public abstract String message();

    public static GoProCommandResponse create(long requestId, boolean success, String message) {
        return new AutoValue_GoProCommandResponse(requestId, success, message);
    }

    public static JsonAdapter<GoProCommandResponse> jsonAdapter(Moshi moshi) {
        return new AutoValue_GoProCommandResponse.MoshiJsonAdapter(moshi);
    }
}