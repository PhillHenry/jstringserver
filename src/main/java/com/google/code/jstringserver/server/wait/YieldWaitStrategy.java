package com.google.code.jstringserver.server.wait;

public class YieldWaitStrategy implements WaitStrategy {

    @Override
    public boolean pause() {
        Thread.yield();
        return true;
    }


}
