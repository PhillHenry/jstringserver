package com.google.code.jstringserver.server;

import org.junit.After;

public abstract class AbstractMultiThreadedTest {
    
    private final ThreadedTaskBuilder threadedTaskBuilder = new ThreadedTaskBuilder();
    
    
    protected void join(Thread[] threads, long timeout) throws InterruptedException {
        threadedTaskBuilder.join(threads, timeout);
    }

    protected Thread[] start(Runnable[] connectors, String name) {
        return threadedTaskBuilder.start(connectors, name);
    }

    protected Thread[] toThreads(Runnable[] connectors, String name) {
        return threadedTaskBuilder.toThreads(connectors, name);
    }
    
    @After
    public void interruptAllThreads() {
        threadedTaskBuilder.interruptAllThreads();
    }

}
