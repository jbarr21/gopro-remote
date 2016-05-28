package com.github.jbarr21.goproremote.common.data;

import com.github.jbarr21.goproremote.common.utils.GoProStateParser;
import com.google.auto.value.AutoValue;

import java.util.Arrays;

/**
 * Legend: https://github.com/KonradIT/goprowifihack/blob/master/ByteStates.md
 *
 * b# | value
 * ---|--------
 * 0 | ?
 * 1 | Current mode. 0-4 matches set CM. 7 - in menu.
 * 2 | ?
 * 3 | Start up mode : 0 = video - 1 = photo - 2 = burst - 3 = timelapse
 * 4 | Spot meter : 0 = Off - 1 = On
 * 5 | Current timelapse interval
 * 6 | Automatic power off : 0 = never - 1 = 60sec - 2 = 120sec - 3 = 300sec
 * 7 | Current view angle
 * 8 | Current photo mode
 * 9 | Current video mode
 * 10 | ?
 * 11 | ?
 * 12 | ?
 * 13 | Recording minutes
 * 14 | Recording seconds
 * 15 | ?
 * 16 | Current beep volume
 * 17 | 2 = 4 LEDS - 1 = 2 LEDS - 0 = LEDS off
 * 18 | bit 1 : 1 = preview on - 0 = preview off / bit 2 : ? / bit 3 : 0 = up - 1 = down / bit 4 : 1 = one button on - 0 = one button off / bit 5 : 1 = OSD on - 0 = OSD off / bit 6 : 0 = NTSC - 1 = PAL / bit 7 : 1 = Locate(beeping) / bit 8 : ?
 * 19 | Battery %
 * 20 | ?
 * 21 | Photos available (hi byte) or 255 = no SD Card
 * 22 | Photos available (lo byte)
 * 23 | Photo count (hi byte)
 * 24 | Photo count (lo byte)
 * 25 | Video Time Remaining in minutes (hi byte)
 * 26 | Video Time Left (lo byte)
 * 27 | Video count (hi byte)
 * 28 | Video count (lo byte)
 * 29 | Recording
 * 30 | ?
 */
@AutoValue
public abstract class GoProState {

    public static final GoProState CAMERA_OFF = GoProStateParser.cameraOff();

    private static final int NO_SD_CARD = 255;

    public abstract byte[] rawState();
    public abstract GoProMode currentMode();
    public abstract GoProMode startupMode();
    public abstract int recordingMinutes();
    public abstract int recordingSeconds();
    public abstract int beepVolume();
    public abstract LedState ledState();
    //public abstract boolean previewEnabled();
    public abstract Orientation orientation();
    public abstract int batteryPercent(); // 99 for 99%
    public abstract int photosAvailable();
    public abstract int photoCount();
    public abstract int videoAvailableMinutes();
    public abstract int videoCount();
    public abstract boolean recording();

    public static Builder builder() {
        return new AutoValue_GoProState.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder rawState(byte[] rawState);
        public abstract Builder currentMode(GoProMode mode);
        public abstract Builder startupMode(GoProMode mode);
        public abstract Builder recordingMinutes(int minutes);
        public abstract Builder recordingSeconds(int seconds);
        public abstract Builder beepVolume(int volume);
        public abstract Builder ledState(LedState state);
        //public abstract Builder previewEnabled(boolean enabled);
        public abstract Builder orientation(Orientation orientation);
        public abstract Builder batteryPercent(int percent); // 99 for 99%
        public abstract Builder photosAvailable(int photos);
        public abstract Builder photoCount(int count);
        public abstract Builder videoAvailableMinutes(int availableMinutes);
        public abstract Builder videoCount(int count);
        public abstract Builder recording(boolean recording);
        public abstract GoProState build();
    }

    public boolean isPowerOn() {
        return this != CAMERA_OFF;
    }

    // TODO: confirm this or if just high-byte (of 2) must be 255
    public boolean hasSdCard() {
        return photosAvailable() == NO_SD_CARD;
    }

    public String logRawState() {
        return String.format("camera current state: " + Arrays.toString(rawState()));
    }

    public boolean isRecordingCurrently() {
        return recording() && currentMode() != GoProMode.BURST;
    }
}
