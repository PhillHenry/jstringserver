package com.google.code.jstringserver.stats;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ThreadLocalStopWatchTest {
    
    private ThreadLocalStopWatch toTest;

    @Test
    public void test() {
        ThreadLocalStopWatch toTest = new ThreadLocalStopWatch("name", 17);
        Assert.assertEquals(31, toTest.getSampleSize());
    }

}
