package com.github.jbarr21.goproremote.common.utils;

import com.github.jbarr21.goproremote.common.data.GoProMode;
import com.github.jbarr21.goproremote.common.data.GoProState;
import com.github.jbarr21.goproremote.common.data.LedState;
import com.github.jbarr21.goproremote.common.data.Orientation;

public class GoProStateParser {
    public static GoProState from(byte[] bytes) {
        if (bytes.length > 30) {
            // Camera is On
            return GoProState.builder()
                    .rawState(bytes)
                    .currentMode(GoProMode.values()[bytes[1]])
                    .startupMode(GoProMode.values()[bytes[3]])
                    .recordingMinutes(bytes[13])
                    .recordingSeconds(bytes[14])
                    .beepVolume(bytes[16])
                    .ledState(LedState.values()[bytes[17]])
                    .orientation(Orientation.UP) // TODO: figure out how to get this
                    .batteryPercent(bytes[19])
                    .photosAvailable(toInt(bytes[21], bytes[22]))
                    .photoCount(toInt(bytes[23], bytes[24]))
                    .videoAvailableMinutes(toInt(bytes[25], bytes[26]))
                    .videoCount(toInt(bytes[27], bytes[28]))
                    .recording(bytes[29] > 0)
                    .build();
        } else {
            // Camera is OFF
            return GoProState.CAMERA_OFF;
        }
    }

    public static GoProState cameraOff() {
        return GoProState.builder()
                .rawState(new byte[0])
                .currentMode(GoProMode.VIDEO)
                .startupMode(GoProMode.VIDEO)
                .recordingMinutes(0)
                .recordingSeconds(0)
                .beepVolume(0)
                .ledState(LedState.NONE)
                .orientation(Orientation.UP)
                .batteryPercent(0)
                .photosAvailable(0)
                .photoCount(0)
                .videoAvailableMinutes(0)
                .videoCount(0)
                .recording(false)
                .build();
    }

    static int toInt(byte highByte, byte lowByte) {
        return highByte << 8 | lowByte;
    }
}
