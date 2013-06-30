package com.google.code.jstringserver.server.wait;

public class NoOpWaitStrategy implements WaitStrategy {

    @Override
    public boolean pause() {
        return true;
    }

}
