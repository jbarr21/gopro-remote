package com.github.jbarr21.goproremote.common.data;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import com.github.jbarr21.goproremote.common.R;

public enum GoProMode {
    VIDEO (0, R.string.video, R.drawable.ic_videocam_white_24dp, GoProCommand.SET_VIDEO),
    PHOTO (1, R.string.photo, R.drawable.ic_photo_camera_white_24dp, GoProCommand.SET_PHOTO),
    BURST (2, R.string.burst, R.drawable.ic_photo_library_white_24dp, GoProCommand.SET_BURST),
    TIMELAPSE (3, R.string.timelapse, R.drawable.ic_timelapse_white_24dp, GoProCommand.SET_TIMELAPSE);

    int index;
    int labelResId;
    int iconResId;
    GoProCommand command;

    GoProMode(int index, @StringRes int labelResId, @DrawableRes int iconResId, GoProCommand command) {
        this.index = index;
        this.labelResId = labelResId;
        this.iconResId = iconResId;
        this.command = command;
    }

    private int getIndex() {
        return index;
    }

    @StringRes
    public int getLabelResId() {
        return labelResId;
    }

    @DrawableRes
    public int getIconResId() {
        return iconResId;
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