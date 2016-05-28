package com.github.jbarr21.goproremote.common.data;

import com.google.auto.value.AutoValue;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

@AutoValue
public abstract class GoProCommandRequest {
    public abstract GoProCommand command();
    public abstract long timestamp();

    public static GoProCommandRequest create(GoProCommand command, long timestamp) {
        return new AutoValue_GoProCommandRequest(command, timestamp);
    }

    public static JsonAdapter<GoProCommandRequest> jsonAdapter(Moshi moshi) {
        return new AutoValue_GoProCommandRequest.MoshiJsonAdapter(moshi);
    }

    public long id() {
        return command() != null ? command().getId() : GoProCommand.UNKNOWN.getId();
    }

}