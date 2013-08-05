package com.google.code.jstringserver.stats;


public class ThreadLocalStopWatch implements Stopwatch {

    private final ThreadLocalStats stats;
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
        long totalCallsServiced = stats.getTotalCallsServiced();
        long totalTimeTaken = stats.getTotalTimeTaken();
        return "ThreadLocalStopWatch [" 
            + "name="
            + name
            + ", totalCallsServiced="
            + totalCallsServiced
            + ", totalTimeTaken="
            + totalTimeTaken
            + ", max time = " + stats.getMaxTime() + timeUnit()
            + ", Average time = " + (totalCallsServiced == 0 ? "NA" : calcAverage(totalCallsServiced, totalTimeTaken))
            + "]";
    }

    protected String timeUnit() {
        return "ms";
    }
    
    public long getMaxTime() {
        return stats.getMaxTime();
    }
    
    public double getAverageInMicroSeconds() {
        long totalCallsServiced = stats.getTotalCallsServiced();
        long totalTimeTaken = stats.getTotalTimeTaken();
        return (totalTimeTaken * 1000 / totalCallsServiced);
    }

    protected String calcAverage(long totalCallsServiced, long totalTimeTaken) {
        return (totalTimeTaken * 1000 / totalCallsServiced) + "us";
    }
}
