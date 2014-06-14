package com.google.code.jstringserver.stats;


public class ThreadLocalStopWatch implements Stopwatch {

    private final Stats  stats;
    private final String name;
    
    private final ThreadLocal<Long> startTime          = new ThreadLocal<>();

    public ThreadLocalStopWatch(
        String name,
        int sampleSizeHint) {
        super();
        this.name = name;
        this.stats = new ThreadLocalStats(sampleSizeHint);
    }

    @Override
    public void start() {
        long timeMillis = getTime();
        stats.start(timeMillis);
        startTime.set(timeMillis);
    }

    protected long getTime() {
        return System.currentTimeMillis();
    }
    
    @Override
    public void stop() {
        stats.stop(getTime() - startTime.get());
    }

    @Override
    public String toString() {
        return "ThreadLocalStopWatch [" 
            + "name="
            + name
            + ", totalCallsServiced="
            + stats.getTotalCallsServiced()
            + ", " + stats
            + ", max time = " + stats.getMaxTime() + timeUnit()
            
            + "]";
    }

    protected String timeUnit() {
        return "ms";
    }
    
    public long getMaxTime() {
        return stats.getMaxTime();
    }

}
