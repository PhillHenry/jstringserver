package com.google.code.jstringserver.stats;


public class ThreadLocalStopWatch implements Stopwatch {

    private final Stats  stats;
    private final String name;
    
    private final ThreadLocal<Long> startTime          = new ThreadLocal<>();

    public ThreadLocalStopWatch(
            String name,
            Stats  stats) {
        super();
        this.name = name;
        this.stats = stats;
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
            + ", max time = " + stats.getMaxTime() + timeUnit()
            + ", " + stats
            
            + "]";
    }

    protected String timeUnit() {
        return "ms";
    }
    
    public long getMaxTime() {
        return stats.getMaxTime();
    }

}
