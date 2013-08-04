package com.google.code.jstringserver.stats;

public class ThreadLocalNanoStopWatch extends ThreadLocalStopWatch {

    public ThreadLocalNanoStopWatch(
        String name,
        int sampleSizeHint) {
        super(name, sampleSizeHint);
    }

    @Override
    protected long getTime() {
        return System.nanoTime();
    }

    protected String calcAverage(long totalCallsServiced, long totalTimeTaken) {
        return (totalTimeTaken / totalCallsServiced) + "us";
    }
    
    protected String timeUnit() {
        return "ns";
    }

}
