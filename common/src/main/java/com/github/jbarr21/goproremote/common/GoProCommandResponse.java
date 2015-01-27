package com.github.jbarr21.goproremote.common;

public class GoProCommandResponse {
    private long requestId;
    private boolean success;
    private String message;

    public GoProCommandResponse(long requestId, boolean success, String message){
        this.requestId = requestId;
        this.success = success;
        this.message = message;
    }

    public long getRequestId() {
        return requestId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}