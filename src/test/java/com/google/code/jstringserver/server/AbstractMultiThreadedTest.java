package com.google.code.jstringserver.server;

import java.util.HashSet;
import java.util.Set;

import org.junit.After;

public abstract class AbstractMultiThreadedTest {
    
    private final Set<Thread> createdThreads = new HashSet<Thread>();;
    
    protected void join(Thread[] threads, long timeout) throws InterruptedException {
        for (int i = 0 ; i < threads.length ; i++) {
            threads[i].join(timeout);
        }
    }

    protected Thread[] start(Runnable[] connectors, String name) {
        Thread[] threads = toThreads(connectors, name);
        for (int i = 0 ; i < threads.length ; i++) {
            threads[i].start();
        }
        return threads;
    }

    protected Thread[] toThreads(Runnable[] connectors, String name) {
        Thread[] threads = new Thread[connectors.length];
        for (int i = 0 ; i < connectors.length ; i++) {
            Thread thread = new Thread(connectors[i], name + i);
            createdThreads.add(thread);
            threads[i] = thread;
        }
        return threads;
    }
    
    @After
    public void interruptAllThreads() {
        for (Thread thread : createdThreads) {
            thread.interrupt();
        }
    }

}
