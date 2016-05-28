package com.github.jbarr21.goproremote.common.utils;

import com.ryanharter.auto.value.moshi.AutoValueMoshiAdapterFactory;
import com.squareup.moshi.Moshi;

public final class Parser {
    private static Moshi moshi;

    private Parser() {
        // no op
    }

    public static Moshi getMoshi() {
        if (moshi == null) {
            moshi = new Moshi.Builder()
                    .add(new AutoValueMoshiAdapterFactory())
                    .build();
        }
        return moshi;
    }
}
