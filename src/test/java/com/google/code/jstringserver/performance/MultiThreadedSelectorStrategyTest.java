package com.google.code.jstringserver.performance;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.google.code.jstringserver.server.handlers.ClientDataHandler;
import com.google.code.jstringserver.server.nio.select.AbstractNioReader;
import com.google.code.jstringserver.server.nio.select.AbstractNioWriter;
import com.google.code.jstringserver.server.nio.select.MultiThreadedReadingSelectionStrategy;
import com.google.code.jstringserver.server.nio.select.NioReaderLooping;
import com.google.code.jstringserver.server.nio.select.SelectionStrategy;
import com.google.code.jstringserver.server.threads.NamedThreadFactory;
import com.google.code.jstringserver.server.wait.SleepWaitStrategy;

public class MultiThreadedSelectorStrategyTest extends SelectorStrategyTest {
    
    private static ExecutorService executorService;

    @BeforeClass
    public static void startExecutor() {
        int poolSize = availableProcessors();
        executorService = new ThreadPoolExecutor(
            poolSize, 
            poolSize, 
            Long.MAX_VALUE, 
            TimeUnit.SECONDS, 
            new LinkedBlockingQueue<Runnable>(Integer.MAX_VALUE),
            new NamedThreadFactory(MultiThreadedReadingSelectionStrategy.class.getSimpleName()));
    }

    @AfterClass
    public static void stopExecutor() {
        executorService.shutdownNow();
    }
    
    @Override
    protected SelectionStrategy createSelectionStrategy(AbstractNioWriter writer, AbstractNioReader reader) {
        return new MultiThreadedReadingSelectionStrategy(null, clientSelector, writer, reader, executorService);
    }

    @Override
    protected AbstractNioReader createNioReader(ClientDataHandler clientDataHandler) {
        return new NioReaderLooping(clientDataHandler, getByteBufferStore(), 10000L, new SleepWaitStrategy(1));
    }
    
}
