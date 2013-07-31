package com.google.code.jstringserver.stats;

public class ThreadLocalStopWatch implements Stopwatch {
    
    private final ThreadLocal<Long> totalTime = new ThreadLocal<Long>();
    
    private final ThreadLocal<Long> startTime = new ThreadLocal<>();
    
    private final ThreadLocal<Long> callsServiced = new ThreadLocal<Long>() {
        @Override
        protected Long initialValue() {
            return 0L;
        }
    };

    @Override
    public void start() {
        startTime.set(System.currentTimeMillis());
    }

    @Override
    public void stop() {
        totalTime.set(System.currentTimeMillis() - startTime.get());
        callsServiced.set(callsServiced.get() + 1);
    }

}
