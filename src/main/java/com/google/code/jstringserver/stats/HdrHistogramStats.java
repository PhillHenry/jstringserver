package com.google.code.jstringserver.stats;

import org.HdrHistogram.Histogram;

public class HdrHistogramStats implements Stats {
    
    final Histogram histogram = new Histogram(60000, 0);

    @Override
    public void start(long timeDeleteMe) {
        // NoOp
    }

    @Override
    public void stop(long duration) {
        histogram.recordValueWithCount(duration, 1);
    }

    @Override
    public long getMaxTime() {
        return histogram.getMaxValue();
    }

    @Override
    public long getTotalCallsServiced() {
        return histogram.getTotalCount();
    }
    
    public String String() {
        return "Mean = " + histogram.getMean()
                + ", Min = " +histogram.getMinValue()
                + ", standard deviation " + histogram.getStdDeviation();
    }

}
