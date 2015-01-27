package com.github.jbarr21.goproremote.common;

public enum GoProCommand {
    POWER_OFF(0,            "Powered Off",                  "power off"),
    POWER_ON(1,             "Powered On",                   "power on"),
    SET_VIDEO(10,           "Set to Video",                 "set to video"),
    SET_PHOTO(11,           "Set to Photo",                 "set to photo"),
    SET_BURST(12,           "Set to Burst",                 "set to burst"),
    SET_TIMELAPSE(13,       "Set to Timelapse",             "set to timelapse"),
    START_RECORDING(100,    "Started Recording",            "start recording"),
    STOP_RECORDING(101,     "Stopped Recording",            "stop recording"),
    TAKE_PHOTO(1001,        "Took Photo",                   "take photo"),
    CONNECT_WIFI(10000,     "Connected to Wi-Fi",           "connect to Wi-Fi"),
    GET_STATE(20000,        "Updated Camera State",         "update camera state"),
    UNKNOWN(-1,             "Completed Unknown Command",    "complete unknown command");

    private int commandId;
    private String successMessage;
    private String failureMessage;

    GoProCommand(int commandId, String successMessage, String failureMessage) {
        this.commandId = commandId;
        this.successMessage = successMessage;
        this.failureMessage = "Failed to " + failureMessage;
    }

    public int getId() {
        return commandId;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public static GoProCommand from(int commandId) {
        for (GoProCommand command : GoProCommand.values()) {
            if (commandId == command.getId()) {
                return command;
            }
        }
        return UNKNOWN;
    }
}