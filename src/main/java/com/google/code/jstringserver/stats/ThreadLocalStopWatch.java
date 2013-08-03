package com.google.code.jstringserver.stats;

import static java.lang.Integer.highestOneBit;

import java.util.concurrent.atomic.AtomicLong;

public class ThreadLocalStopWatch implements Stopwatch {

    private final AtomicLong        totalCallsServiced = new AtomicLong();
    private final AtomicLong        totalTimeTaken     = new AtomicLong();

    private final ThreadLocal<Long> cumulativeTime          = new ThreadLocal<Long>();

    private final ThreadLocal<Long> startTime          = new ThreadLocal<>();

    private final ThreadLocal<Long> callsServiced      = new ThreadLocal<Long>() {
        @Override
        protected Long initialValue() {
                return 0L;
            }
        };

    private final String            name;
    private final int               sampleSize;

    public ThreadLocalStopWatch(
        String name,
        int sampleSizeHint) {
        super();
        this.name = name;
        int leftMost = highestOneBit(sampleSizeHint);
        this.sampleSize = (leftMost << 1) - 1;
    }

    @Override
    public void start() {
        startTime.set(System.currentTimeMillis());
        long calls = callsServiced.get().longValue();
        if ((calls & sampleSize) > 0) {
            totalCallsServiced.addAndGet(calls);
            callsServiced.set(0L);
            totalTimeTaken.addAndGet(cumulativeTime.get());
            cumulativeTime.set(0L);
        }
    }

    @Override
    public void stop() {
        cumulativeTime.set(System.currentTimeMillis() - startTime.get());
        callsServiced.set(callsServiced.get() + 1);
    }

    int getSampleSize() {
        return sampleSize;
    }

    @Override
    public String toString() {
        long totalCallsServiced = this.totalCallsServiced.longValue();
        long totalTimeTaken = this.totalTimeTaken.longValue();
        return "ThreadLocalStopWatch [" 
            + "name="
            + name
            + ", totalCallsServiced="
            + totalCallsServiced
            + ", totalTimeTaken="
            + totalTimeTaken
            + ", Average time = " + (totalCallsServiced == 0 ? "NA" : (totalTimeTaken * 1000 / totalCallsServiced) + "us")
            + "]";
    }
}
