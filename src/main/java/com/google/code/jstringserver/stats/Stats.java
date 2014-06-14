package com.google.code.jstringserver.stats;

public interface Stats {

    public abstract void start(long timeDeleteMe);

    public abstract void stop(long duration);

    public abstract long getMaxTime();

    public abstract long getTotalCallsServiced();

}