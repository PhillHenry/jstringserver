package com.google.code.jstringserver.stats;

import org.HdrHistogram.Histogram;

public class CsvLinearHistogramFormatStrategy extends
		LinearHistogramFormatStrategy {

	public CsvLinearHistogramFormatStrategy(int maxReading) {
		super(maxReading);
	}

	@Override
	protected void output(StringBuffer szb, int index, int step,
			Histogram histogram, int max) {
		int highValue = index + step;
    	szb.append(histogram.getCountBetweenValues(index, highValue));
    	if (index < (max - step)) {
    		szb.append(", ");
    	}
	}

}
