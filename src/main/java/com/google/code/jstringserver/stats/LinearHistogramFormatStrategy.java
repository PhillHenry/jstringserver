package com.google.code.jstringserver.stats;

import org.HdrHistogram.Histogram;

public class LinearHistogramFormatStrategy implements HistogramFormatStrategy {
    
    private final int maxReading;
    
    private final StringBuffer szb = new StringBuffer();
    
    private final int numSteps = 20;

	private final int step;
    
    public LinearHistogramFormatStrategy(int maxReading) {
        super();
        this.maxReading = maxReading;
        step = (maxReading) / numSteps;
    }

    @Override
    public String format(Histogram histogram) {
        szb.delete(0, szb.length());
        for (int index = 0 ; index < maxReading ; index += step) {
        	output(szb, index, step, histogram, maxReading);
        }
        return szb.toString();
    }

    protected void output(StringBuffer szb, int low, int step, Histogram histogram, int max) {
    	int highValue = low + step;
    	szb.append(low).append("-").append(highValue).append(": ~")
    		.append(histogram.getCountBetweenValues(low, highValue)).append("\n");
    }

	@Override
	public String metadata() {
		return "Histogram with stepsize = " + step + ". Approximate values";
	}
    
}
