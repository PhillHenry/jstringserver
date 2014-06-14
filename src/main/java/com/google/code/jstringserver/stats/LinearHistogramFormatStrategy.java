package com.google.code.jstringserver.stats;

import org.HdrHistogram.Histogram;

public class LinearHistogramFormatStrategy implements HistogramFormatStrategy {
    
    private final int maxReading;
    
    private final StringBuffer szb = new StringBuffer(); 
    
    public LinearHistogramFormatStrategy(int maxReading) {
        super();
        this.maxReading = maxReading;
    }

    @Override
    public String format(Histogram histogram) {
        szb.delete(0, szb.length());
        int step = (maxReading) / 10;
        for (int i = 0 ; i < maxReading ; i+= step) {
            int highValue = i + step;
            szb.append(i).append("-").append(highValue).append(": ~").append(histogram.getCountBetweenValues(i, highValue)).append("\n");
        }
        return szb.toString();
    }

}
