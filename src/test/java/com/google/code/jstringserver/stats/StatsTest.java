package com.google.code.jstringserver.stats;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StatsTest {
    
    private ThreadLocalStopWatch toTest;

    @Test
    public void test() {
        ThreadLocalStats toTest = new ThreadLocalStats(17);
        assertEquals(31, toTest.getSampleSize());
    }

}
