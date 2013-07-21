package com.google.code.jstringserver.server.threads;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
    
    private final String prepend;
    
    private final AtomicInteger threadId = new AtomicInteger(-1);

    public NamedThreadFactory(
        String prepend) {
        super();
        this.prepend = prepend;
    }

    @Override
    public Thread newThread(Runnable target) {
        return new Thread(target, prepend + "_" + threadId.incrementAndGet());
    }

}
