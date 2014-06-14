package com.google.code.jstringserver.stats;

import static java.lang.Long.highestOneBit;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class HdrHistogramStatsTest {
    
    private HdrHistogramStats toTest;
    
    @Before
    public void setUp() {
        toTest = new HdrHistogramStats();
    }

    @Test
    public void testMaxSmallRange() {
        toTest.stop(1);
        toTest.stop(2);
        toTest.stop(3);
        toTest.stop(4);
        assertEquals(4, toTest.getMaxTime());
    }
    
    @Test
    public void testMaxLargeRange() {
        int max = 10000;
        for (int i = 0 ; i < max ; i++) {
            toTest.stop(i);
        }
        long expectedMax = highestOneBit(max);
        assertEquals(expectedMax, toTest.getMaxTime());
        assertEquals(max, toTest.getTotalCallsServiced());
    }

}
