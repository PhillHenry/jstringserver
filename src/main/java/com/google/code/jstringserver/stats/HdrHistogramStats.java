package com.google.code.jstringserver.stats;

import static com.google.code.jstringserver.stats.HistogramTimer.neverHistogramTimer;

import org.HdrHistogram.AbstractHistogram.RecordedValues;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.HistogramIterationValue;

public class HdrHistogramStats implements Stats {
    
    public static final int MAX_READING = 60000;
    
    private final Histogram histogram = new Histogram(MAX_READING, 0);

    private final StringBuffer szb = new StringBuffer();
    
    private final HistogramTimer histogramTimer;

    public HdrHistogramStats() {
        this(neverHistogramTimer);
    }
    
    public HdrHistogramStats(HistogramTimer histogramTimer) {
        this.histogramTimer = histogramTimer;
    }

    @Override
    public void start(long timeDeleteMe) {
        // NoOp
    }

    @Override
    public void stop(long duration) {
        try {
            histogram.recordValueWithCount(duration, 1);
        } catch (ArrayIndexOutOfBoundsException x) { }
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
                + ", standard deviation " + histogram.getStdDeviation()
                + (histogramTimer.isTimeForHistogram() ? histogram() : "");
    }
    
    String histogram() {
        szb.delete(0, szb.length());
        RecordedValues recordedValues = histogram.recordedValues();
        
        for (HistogramIterationValue value : recordedValues) {
            szb.append(value.getPercentile()).append(": ")
                .append(value.getCountAddedInThisIterationStep()).append("\n");
        }
        
//        {
//            szb.append(rangeStart).append(" : ").append(histogram.)
//            power++;
//        }
        return szb.toString();
        
    }

    
}
