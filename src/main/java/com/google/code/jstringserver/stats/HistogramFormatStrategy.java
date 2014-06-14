package com.google.code.jstringserver.stats;

import org.HdrHistogram.Histogram;

public interface HistogramFormatStrategy {
    
    public HistogramFormatStrategy noOpHistogramFormatStrategy = new HistogramFormatStrategy() {

        @Override
        public String format(Histogram histogram) {
            return "";
        }
        
    };
    
    public String format(Histogram histogram);
}
