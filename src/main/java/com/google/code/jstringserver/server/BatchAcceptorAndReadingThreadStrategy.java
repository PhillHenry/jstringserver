package com.google.code.jstringserver.server;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.concurrent.ExecutorService;

import com.google.code.jstringserver.server.nio.select.AbstractNioReader;
import com.google.code.jstringserver.server.nio.select.AbstractNioWriter;
import com.google.code.jstringserver.server.nio.select.BatchServerAndReadingSelectionStrategy;
import com.google.code.jstringserver.server.nio.select.ChunkedReaderWriter;
import com.google.code.jstringserver.server.nio.select.NioReader;
import com.google.code.jstringserver.server.nio.select.ReaderWriter;
import com.google.code.jstringserver.server.nio.select.ReaderWriterFactory;
import com.google.code.jstringserver.server.wait.NoOpWaitStrategy;
import com.google.code.jstringserver.server.wait.WaitStrategy;
import com.google.code.jstringserver.stats.Stopwatch;

public class BatchAcceptorAndReadingThreadStrategy implements ThreadStrategy {
    
    private final BatchServerAndReadingSelectionStrategy    batchSelectionStrategy;
    private final ExecutorService                           executor;
    private final int                                       numThreads;
    
    private volatile boolean isRunning = true;

    public BatchAcceptorAndReadingThreadStrategy(
        final Server            server,
        final AbstractNioReader reader, 
        final ThreadPoolFactory threadPoolFactory, 
        final AbstractNioWriter writer, 
        final Stopwatch         stopWatch,
        final Stopwatch         readerWriterStopwatch) throws IOException {
        super();
        executor                    = threadPoolFactory.createThreadPoolExecutor();
        final Selector selector     = Selector.open();
        server.register(selector);
        ReaderWriterFactory readerWriterFactory = new ReaderWriterFactory() {

            @Override
            public ReaderWriter createReaderWriter() {
                return new ChunkedReaderWriter((NioReader) reader, writer, readerWriterStopwatch);
            }
            
        };
        WaitStrategy waitStrategy   = new NoOpWaitStrategy(); // createWaitStrategy(selector);
        batchSelectionStrategy      = new BatchServerAndReadingSelectionStrategy(
            waitStrategy, 
            selector, 
            readerWriterFactory, 
            stopWatch);
        numThreads                  = threadPoolFactory.getNumThreads();
    }

    private WaitStrategy createWaitStrategy(final Selector selector) {
        return new WaitStrategy() {
            
            @Override
            public boolean pause() {
                selector.wakeup();
                return true;
            }
        };
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
