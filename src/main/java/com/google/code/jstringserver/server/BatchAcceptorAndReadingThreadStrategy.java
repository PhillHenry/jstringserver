package com.google.code.jstringserver.server;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.concurrent.ExecutorService;

import com.google.code.jstringserver.server.nio.select.AbstractNioReader;
import com.google.code.jstringserver.server.nio.select.AbstractNioWriter;
import com.google.code.jstringserver.server.nio.select.BatchServerAndReadingSelectionStrategy;
import com.google.code.jstringserver.server.wait.WaitStrategy;

public class BatchAcceptorAndReadingThreadStrategy implements ThreadStrategy {
    
    private final BatchServerAndReadingSelectionStrategy    batchSelectionStrategy;
    private final ExecutorService                           executor;
    private final int                                       numThreads;
    
    private volatile boolean isRunning = true;

    public BatchAcceptorAndReadingThreadStrategy(
        Server            server,
        AbstractNioReader reader, 
        ThreadPoolFactory threadPoolFactory, 
        AbstractNioWriter writer) throws IOException {
        super();
        executor                    = threadPoolFactory.createThreadPoolExecutor();
        final Selector selector     = Selector.open();
        server.register(selector);
        WaitStrategy waitStrategy = new WaitStrategy() {
            
            @Override
            public boolean pause() {
                selector.wakeup();
                return true;
            }
        };
        batchSelectionStrategy      = new BatchServerAndReadingSelectionStrategy(
            waitStrategy, 
            selector, 
            reader, 
            writer);
        numThreads                  = threadPoolFactory.getNumThreads();
    }

    @Override
    public void start() throws Exception {
        for (int i = 0 ; i < numThreads ; i++) {
            executor.submit(new Runnable() {
                
                @Override
                public void run() {
                    while (isRunning) {
                        try {
                            batchSelectionStrategy.select();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void shutdown() {
        isRunning = false;
        executor.shutdown();
    }

}
