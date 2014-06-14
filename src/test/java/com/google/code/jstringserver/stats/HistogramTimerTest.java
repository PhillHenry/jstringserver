package com.google.code.jstringserver.stats;

import static com.google.code.jstringserver.stats.HistogramTimer.threadLocalEvery8th;
import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

public class HistogramTimerTest {

    @Test
    public void test() {
        for (int i = 0 ; i < 7 ; i ++) {
            assertFalse("" + i, threadLocalEvery8th.isTimeForHistogram());
        }
        Assert.assertTrue(threadLocalEvery8th.isTimeForHistogram());
    }

}
