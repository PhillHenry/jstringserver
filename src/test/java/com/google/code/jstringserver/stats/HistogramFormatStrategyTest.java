package com.google.code.jstringserver.stats;

import static org.junit.Assert.*;

import org.HdrHistogram.Histogram;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HistogramFormatStrategyTest {

    private HistogramFormatStrategy toTest;
    private int max = 10000;
    private Histogram histogram;
    
    @Before
    public void setUp() {
        histogram = new Histogram(max, 1);
    }
    
    @Test
    public void testPercentileHistogramFormatStrategy() {
        toTest = new PercentileHistogramFormatStrategy();
        histogram();
    }
    
    @Test
    public void testLinearHistorgramFormatStrategy() {
        toTest = new LinearHistogramFormatStrategy(max);
        histogram();
    }
    
    public void histogram() {
        
        populateUniformallyTo(max);
        String first = toTest.format(histogram);
        String firstAgain = toTest.format(histogram);
        assertEquals(first, firstAgain);
        
        populateUniformallyTo(max);
        String second = toTest.format(histogram);
        Assert.assertNotEquals(first, second);
        System.out.println(first);
    }
    
    private void populateUniformallyTo(int max) {
        for (int i = 0 ; i < max ; i++) {
            histogram.recordValueWithCount(i, 1);
        }
    }

}
