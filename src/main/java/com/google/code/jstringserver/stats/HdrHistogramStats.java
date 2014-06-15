package com.google.code.jstringserver.stats;

import static com.google.code.jstringserver.stats.HistogramFormatStrategy.noOpHistogramFormatStrategy;
import static com.google.code.jstringserver.stats.HistogramTimer.neverHistogramTimer;

import org.HdrHistogram.Histogram;

public class HdrHistogramStats implements Stats {
    
    public static final int MAX_READING = 60000;
    
    private final Histogram histogram = new Histogram(MAX_READING, 0);
    
    private final HistogramTimer histogramTimer;
    
    private final HistogramFormatStrategy histogramFormatStrategy;

    public HdrHistogramStats() {
        this(neverHistogramTimer, noOpHistogramFormatStrategy);
    }
    
    public HdrHistogramStats(HistogramTimer histogramTimer, HistogramFormatStrategy histogramFormatStrategy) {
        this.histogramTimer             = histogramTimer;
        this.histogramFormatStrategy    = histogramFormatStrategy;
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
    	try {
    		return histogram.getMaxValue();
    	} catch (ArrayIndexOutOfBoundsException x) { 
    		return -1;
    	}
    }

    @Override
    public long getTotalCallsServiced() {
        return histogram.getTotalCount();
    }
    
    public String toString() {
        return "Mean = " + histogram.getMean()
                + ", Min = " + histogram.getMinValue()
                + ", standard deviation " + histogram.getStdDeviation()
                + (histogramTimer.isTimeForHistogram() ? 
                		"\n" + histogramFormatStrategy.metadata() + ": " + 
                		histogramFormatStrategy.format(histogram) : "");
    }
    


    
}
