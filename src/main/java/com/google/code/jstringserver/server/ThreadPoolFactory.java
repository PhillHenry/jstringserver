package com.google.code.jstringserver.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolFactory {
    
    private final int numThreads;

    public ThreadPoolFactory(int numThreads) {
        super();
        this.numThreads = numThreads;
    }

    public ExecutorService createThreadPoolExecutor() {
        return new ThreadPoolExecutor(numThreads, numThreads, 0, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());
    }

    public int getNumThreads() {
        return numThreads;
    }

}
