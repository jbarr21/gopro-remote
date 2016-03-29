package com.github.jbarr21.goproremote.common;

public class GoProCommandRequest {
    private final long id;
    private final GoProCommand command;

    public GoProCommandRequest(GoProCommand command) {
        this.id = System.currentTimeMillis();
        this.command = command;
    }

    public long getId() {
        return id;
    }

    public GoProCommand getCommand() {
        return command;
    }
}