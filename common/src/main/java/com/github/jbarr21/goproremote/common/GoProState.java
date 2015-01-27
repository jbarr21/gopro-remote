package com.github.jbarr21.goproremote.common;

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
public class GoProState {

    public static final GoProState CAMERA_OFF = new GoProStateOff();

    private static final int NO_SD_CARD = 255;

    private byte[] rawState;
    private GoProMode currentMode;
    private GoProMode startupMode;
    private int recordingMinutes;
    private int recordingSeconds;
    private int beepVolume;
    private LedState ledState;
    private boolean isPreviewEnabled;
    private Orientation orientation;
    private int batteryPercent; // 99 for 99%
    private int photosAvailable;
    private int photoCount;
    private int videoAvailableMinutes;
    private int videoCount;
    private boolean isRecording;

    private GoProState() { }

    private GoProState(Builder builder) {
        rawState = builder.rawState;
        currentMode = builder.currentMode;
        startupMode = builder.startupMode;
        recordingMinutes = builder.recordingMinutes;
        recordingSeconds = builder.recordingSeconds;
        beepVolume = builder.beepVolume;
        ledState = builder.ledState;
        isPreviewEnabled = builder.isPreviewEnabled;
        orientation = builder.orientation;
        batteryPercent = builder.batteryPercent;
        photosAvailable = builder.photosAvailable;
        photoCount = builder.photoCount;
        videoAvailableMinutes = builder.videoAvailableMinutes;
        videoCount = builder.videoCount;
        isRecording = builder.isRecording;
    }

    public static GoProState from(byte[] bytes) {
        if (bytes.length > 30) {
            // Camera is On
            return new Builder()
                    .rawState(bytes)
                    .currentMode(GoProMode.values()[bytes[1]])
                    .startupMode(GoProMode.values()[bytes[3]])
                    .recordingMinutes(bytes[13])
                    .recordingSeconds(bytes[14])
                    .beepVolume(bytes[16])
                    .ledState(LedState.values()[bytes[17]])
                    .batteryPercent(bytes[19])
                    .photosAvailable(toInt(bytes[21], bytes[22]))
                    .photoCount(toInt(bytes[23], bytes[24]))
                    .videoAvailableMinutes(toInt(bytes[25], bytes[26]))
                    .videoCount(toInt(bytes[27], bytes[28]))
                    .isRecording(bytes[29] > 0)
                    .build();
        } else {
            return GoProState.CAMERA_OFF;
        }
    }

    static int toInt(byte highByte, byte lowByte) {
        return highByte << 8 | lowByte;
    }

    public boolean isPowerOn() {
        return this != CAMERA_OFF;
    }

    // TODO: confirm this or if just high-byte (of 2) must be 255
    public boolean hasSdCard() {
        return photosAvailable == NO_SD_CARD;
    }

    public String logRawState() {
        return String.format("camera current state: " + Arrays.toString(rawState));
    }

    public byte[] getRawState() {
        return rawState;
    }

    public GoProMode getCurrentMode() {
        return currentMode;
    }

    public GoProMode getStartupMode() {
        return startupMode;
    }

    public int getRecordingMinutes() {
        return recordingMinutes;
    }

    public int getRecordingSeconds() {
        return recordingSeconds;
    }

    public int getBeepVolume() {
        return beepVolume;
    }

    public LedState getLedState() {
        return ledState;
    }

    public boolean isPreviewEnabled() {
        return isPreviewEnabled;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public int getBatteryPercent() {
        return batteryPercent;
    }

    public int getPhotosAvailable() {
        return photosAvailable;
    }

    public int getPhotoCount() {
        return photoCount;
    }

    public int getVideoAvailableMinutes() {
        return videoAvailableMinutes;
    }

    public int getVideoCount() {
        return videoCount;
    }

    public boolean isRecording() {
        return isRecording && currentMode != GoProMode.BURST;
    }

    @Override
    public String toString() {
        return "GoProState{" +
                "isRecording=" + isRecording +
                ", currentMode=" + currentMode +
                ", startupMode=" + startupMode +
                ", recordingMinutes=" + recordingMinutes +
                ", recordingSeconds=" + recordingSeconds +
                ", beepVolume=" + beepVolume +
                ", ledState=" + ledState +
                ", isPreviewEnabled=" + isPreviewEnabled +
                ", orientation=" + orientation +
                ", batteryPercent=" + batteryPercent +
                ", photosAvailable=" + photosAvailable +
                ", photoCount=" + photoCount +
                ", videoAvailableMinutes=" + videoAvailableMinutes +
                ", videoCount=" + videoCount +
                '}';
    }

    public static final class Builder {
        private byte[] rawState;
        private GoProMode currentMode;
        private GoProMode startupMode;
        private int recordingMinutes;
        private int recordingSeconds;
        private int beepVolume;
        private LedState ledState;
        private boolean isPreviewEnabled;
        private Orientation orientation;
        private int batteryPercent;
        private int photosAvailable;
        private int photoCount;
        private int videoAvailableMinutes;
        private int videoCount;
        private boolean isRecording;

        public Builder() {
        }

        public Builder rawState(byte[] rawState) {
            this.rawState = rawState;
            return this;
        }

        public Builder currentMode(GoProMode currentMode) {
            this.currentMode = currentMode;
            return this;
        }

        public Builder startupMode(GoProMode startupMode) {
            this.startupMode = startupMode;
            return this;
        }

        public Builder recordingMinutes(int recordingMinutes) {
            this.recordingMinutes = recordingMinutes;
            return this;
        }

        public Builder recordingSeconds(int recordingSeconds) {
            this.recordingSeconds = recordingSeconds;
            return this;
        }

        public Builder beepVolume(int beepVolume) {
            this.beepVolume = beepVolume;
            return this;
        }

        public Builder ledState(LedState ledState) {
            this.ledState = ledState;
            return this;
        }

        public Builder isPreviewEnabled(boolean isPreviewEnabled) {
            this.isPreviewEnabled = isPreviewEnabled;
            return this;
        }

        public Builder orientation(Orientation orientation) {
            this.orientation = orientation;
            return this;
        }

        public Builder batteryPercent(int batteryPercent) {
            this.batteryPercent = batteryPercent;
            return this;
        }

        public Builder photosAvailable(int photosAvailable) {
            this.photosAvailable = photosAvailable;
            return this;
        }

        public Builder photoCount(int photoCount) {
            this.photoCount = photoCount;
            return this;
        }

        public Builder videoAvailableMinutes(int videoAvailableMinutes) {
            this.videoAvailableMinutes = videoAvailableMinutes;
            return this;
        }

        public Builder videoCount(int videoCount) {
            this.videoCount = videoCount;
            return this;
        }

        public Builder isRecording(boolean isRecording) {
            this.isRecording = isRecording;
            return this;
        }

        public GoProState build() {
            return new GoProState(this);
        }
    }

    private static class GoProStateOff extends GoProState {
        private GoProStateOff() {
            super(new Builder());
        }

        @Override
        public GoProMode getCurrentMode() {
            return GoProMode.VIDEO;
        }
    }
}
