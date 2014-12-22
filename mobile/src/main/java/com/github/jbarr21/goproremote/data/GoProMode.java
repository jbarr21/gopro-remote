package com.github.jbarr21.goproremote.data;

public enum GoProMode {
    VIDEO (0),
    PHOTO (1),
    BURST (2),
    TIMELAPSE (3);

    int value;

    GoProMode(int value) {
        this.value = value;
    }
}