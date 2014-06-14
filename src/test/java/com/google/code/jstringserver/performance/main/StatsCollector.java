package com.google.code.jstringserver.performance.main;

import java.util.HashMap;
import java.util.Map;

import com.google.code.jstringserver.stats.HdrHistogramStats;
import com.google.code.jstringserver.stats.Stopwatch;
import com.google.code.jstringserver.stats.ThreadLocalNanoStopWatch;
import com.google.code.jstringserver.stats.ThreadLocalStats;
import com.google.code.jstringserver.stats.ThreadLocalStopWatch;

public class StatsCollector {
    private static final int             SAMPLE_SIZE_HINT = 100;
    private final Map<String, Stopwatch> nameToStopwatch  = new HashMap<>();

    public Stopwatch getStopWatchFor(String name) {
        Stopwatch stopwatch = nameToStopwatch.get(name);
        if (stopwatch == null) {
            stopwatch = new ThreadLocalStopWatch(name, new ThreadLocalStats(SAMPLE_SIZE_HINT));
            nameToStopwatch.put(name, stopwatch);
        }
        return stopwatch;
    }
    
    public Stopwatch getStopWatchWithHistogramFor(String name) {
        Stopwatch stopwatch = nameToStopwatch.get(name);
        if (stopwatch == null) {
            stopwatch = new ThreadLocalStopWatch(name, new HdrHistogramStats());
            nameToStopwatch.put(name, stopwatch);
        }
        return stopwatch;
    }

    public void started() throws InterruptedException {
        System.out.println("Started");
        while (true) {
            for (Stopwatch stopwatch : nameToStopwatch.values()) {
                System.out.println(stopwatch);
            }
            System.out.println();
            Thread.sleep(2000);
        }
    }

    public Stopwatch getNanoStopWatchFor(String name) {
        Stopwatch stopwatch = nameToStopwatch.get(name);
        if (stopwatch == null) {
            stopwatch = new ThreadLocalNanoStopWatch(name, new ThreadLocalStats(SAMPLE_SIZE_HINT));
            nameToStopwatch.put(name, stopwatch);
        }
        return stopwatch;
    }
}
