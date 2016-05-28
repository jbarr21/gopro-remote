package com.github.jbarr21.goproremote.common.data;

public class Constants {

    public static final String SCHEME_WEAR = "wear";

    // Message and Data paths
    public static final String COMMAND_REQUEST_PATH = "/command/request";
    public static final String COMMAND_RESPONSE_PATH = "/command/response";
    public static final String CONFIG_PATH = "/config";

    // Shared timeout values
    public static final int WEARABLE_CONN_TIMEOUT_MILLIS = 500;
    public static final int GO_PRO_COMMAND_TIMEOUT_SEC = 5;

    public static final int TAKE_PHOTO = 10;
    public static final int START_VIDEO = 20;
    public static final int STOP_VIDEO = 21;
    public static final int SWITCH_TO_PHOTO = 1;
    public static final int SWITCH_TO_VIDEO = 2;
    public static final int POWER_ON = 30;
    public static final int POWER_OFF = 31;
}
