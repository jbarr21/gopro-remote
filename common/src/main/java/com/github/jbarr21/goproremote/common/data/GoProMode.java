package com.github.jbarr21.goproremote.common.data;

import android.support.annotation.StringRes;

import com.github.jbarr21.goproremote.common.R;

public enum GoProMode {
    VIDEO (0, R.string.video, GoProCommand.SET_VIDEO),
    PHOTO (1, R.string.photo, GoProCommand.SET_PHOTO),
    BURST (2, R.string.burst, GoProCommand.SET_BURST),
    TIMELAPSE (3, R.string.timelapse, GoProCommand.SET_TIMELAPSE);

    int index;
    int labelResId;
    GoProCommand command;

    GoProMode(int index, @StringRes int labelResId, GoProCommand command) {
        this.index = index;
        this.labelResId = labelResId;
        this.command = command;
    }

    private int getIndex() {
        return index;
    }

    @StringRes
    public int getLabelResId() {
        return labelResId;
    }

    public GoProCommand getCommand() {
        return command;
    }

    public static GoProMode from(int modeIndex) {
        for (GoProMode mode : GoProMode.values()) {
            if (modeIndex == mode.getIndex()) {
                return mode;
            }
        }
        return VIDEO;
    }
}