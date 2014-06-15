package com.google.code.jstringserver.stats;

import org.HdrHistogram.Histogram;

public interface HistogramFormatStrategy {
    
    public HistogramFormatStrategy noOpHistogramFormatStrategy = new HistogramFormatStrategy() {

        @Override
        public String format(Histogram histogram) {
            return "";
        }

		@Override
		public String metadata() {
			return "";
		}
        
    };
    
    public String metadata();
    
    public String format(Histogram histogram);
}
