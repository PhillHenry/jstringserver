package com.google.code.jstringserver.stats;

import static java.lang.Integer.highestOneBit;

import java.util.concurrent.atomic.AtomicLong;

public class ThreadLocalStats {
    private final AtomicLong        totalCallsServiced = new AtomicLong();
    private final AtomicLong        totalTimeTaken     = new AtomicLong();
    private final AtomicLong        overallMaxTime     = new AtomicLong();
    
    private final ThreadLocal<Long> cumulativeTime     = new ThreadLocal<Long>() {
        @Override
        protected Long initialValue() {
                return 0L;
        }
    };
    private final ThreadLocal<Long> callsServiced      = new ThreadLocal<Long>() {
        @Override
        protected Long initialValue() {
                return 0L;
        }
    };
    private final ThreadLocal<Long> maxTime            = new ThreadLocal<Long>() {
        @Override
        protected Long initialValue() {
                return 0L;
        }
    };   
    
    private final int               sampleSize;
    
    public ThreadLocalStats(
        int sampleSizeHint) {
        super();
        int leftMost = highestOneBit(sampleSizeHint);
        this.sampleSize = (leftMost << 1) - 1;
    }
        
    public void start(long timeDeleteMe) {
        long calls = callsServiced.get().longValue();
        if ((calls & sampleSize) > 0) {
            totalCallsServiced.addAndGet(calls);
            callsServiced.set(0L);
            totalTimeTaken.addAndGet(cumulativeTime.get());
            cumulativeTime.set(0L);
            
            long maxTimeAllThreds = overallMaxTime.get();
            while (maxTime.get() > maxTimeAllThreds) {
                maxTimeAllThreds = overallMaxTime.getAndSet(maxTime.get());
            }
        }
    }

    public void stop(long duration) {
        cumulativeTime.set(cumulativeTime.get() + duration);
        callsServiced.set(callsServiced.get() + 1);
        if (duration > maxTime.get()) {
            maxTime.set(duration);
        }
    }

    int getSampleSize() {
        return sampleSize;
    }

    public long getTotalCallsServiced() {
        return totalCallsServiced.longValue();
    }

    public long getTotalTimeTaken() {
        return totalTimeTaken.longValue();
    }
    
    public long getMaxTime() {
        return overallMaxTime.get();
    }
    
}
