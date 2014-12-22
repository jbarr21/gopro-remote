package com.github.jbarr21.goproremote.data;

public enum LedState {
    NONE (0),
    HALF (1),
    ALL (2);

    int value;

    LedState(int value) {
        this.value = value;
    }
}