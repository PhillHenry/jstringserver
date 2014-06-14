package com.google.code.jstringserver.stats;

import org.HdrHistogram.Histogram;
import org.HdrHistogram.HistogramIterationValue;
import org.HdrHistogram.AbstractHistogram.RecordedValues;

public class PercentileHistogramFormatStrategy implements
        HistogramFormatStrategy {
    
    private final StringBuffer szb = new StringBuffer();

    @Override
    public String format(Histogram histogram) {
        szb.delete(0, szb.length());
        RecordedValues recordedValues = histogram.recordedValues();
        
        for (HistogramIterationValue value : recordedValues) {
            szb.append(value.getPercentile()).append(": ")
                .append(value.getCountAddedInThisIterationStep()).append("\n");
        }
        

        return szb.toString();
        
    }

}
