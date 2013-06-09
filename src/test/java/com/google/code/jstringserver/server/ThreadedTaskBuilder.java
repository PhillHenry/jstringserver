package com.google.code.jstringserver.server;

import java.util.HashSet;
import java.util.Set;

public class ThreadedTaskBuilder {

private final Set<Thread> createdThreads = new HashSet<Thread>();;
    
    public void join(Thread[] threads, long timeout) throws InterruptedException {
        for (int i = 0 ; i < threads.length ; i++) {
            threads[i].join(timeout);
        }
    }

    public Thread[] start(Runnable[] connectors, String name) {
        Thread[] threads = toThreads(connectors, name);
        for (int i = 0 ; i < threads.length ; i++) {
            threads[i].start();
        }
        return threads;
    }

    public Thread[] toThreads(Runnable[] connectors, String name) {
        Thread[] threads = new Thread[connectors.length];
        for (int i = 0 ; i < connectors.length ; i++) {
            Thread thread = new Thread(connectors[i], name + i);
            createdThreads.add(thread);
            threads[i] = thread;
        }
        return threads;
    }
    
    public void interruptAllThreads() {
        for (Thread thread : createdThreads) {
            thread.interrupt();
        }
    }

}
