package com.google.code.jstringserver.server.wait;

public class SleepWaitStrategy implements WaitStrategy {
    
    private final long pauseTimeMs;

    public SleepWaitStrategy(long pauseTimeMs) {
        super();
        this.pauseTimeMs = pauseTimeMs;
    }

    @Override
    public boolean pause() {
        try {
            Thread.sleep(pauseTimeMs);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }


}
