package com.google.code.jstringserver.server;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.concurrent.ExecutorService;

import com.google.code.jstringserver.server.nio.select.AbstractNioReader;
import com.google.code.jstringserver.server.nio.select.BatchServerAndReadingSelectionStrategy;

public class BatchAcceptorAndReadingThreadStrategy implements ThreadStrategy {
    
    private final BatchServerAndReadingSelectionStrategy    batchSelectionStrategy;
    private final ExecutorService                           executor;
    private final int                                       numThreads;
    
    private volatile boolean isRunning;

    public BatchAcceptorAndReadingThreadStrategy(
        AbstractNioReader reader, 
        ThreadPoolFactory threadPoolFactory) throws IOException {
        super();
        Selector selector           = Selector.open();
        executor                    = threadPoolFactory.createThreadPoolExecutor();
        batchSelectionStrategy      = new BatchServerAndReadingSelectionStrategy(null, selector, reader);
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
