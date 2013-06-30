package com.google.code.jstringserver.server;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolFactory {

    public ThreadPoolExecutor createThreadPoolExecutor(int numThreads) {
        return new ThreadPoolExecutor(numThreads, numThreads, 0, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());
    }

}
