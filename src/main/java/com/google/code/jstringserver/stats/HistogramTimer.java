package com.google.code.jstringserver.stats;

public interface HistogramTimer {
    
    public static HistogramTimer neverHistogramTimer = new HistogramTimer() {
        public boolean isTimeForHistogram() { return false ; }
    };
    
    public static HistogramTimer alwaysHistogramTimer = new HistogramTimer() {
        public boolean isTimeForHistogram() { return true ; }
    };
    
    public static HistogramTimer threadLocalEvery8th = new HistogramTimer() {
        private final ThreadLocal<Integer> count = new ThreadLocal<Integer>() {

            @Override
            protected Integer initialValue() {
                return 0;
            }
            
        };
        public boolean isTimeForHistogram() { 
            count.set(count.get() + 1);
            return (count.get() & 7) == 0; 
        }
    };

    public boolean isTimeForHistogram();
    
}
