package com.google.code.jstringserver.performance;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;

import com.google.code.jstringserver.server.handlers.ClientDataHandler;
import com.google.code.jstringserver.server.nio.select.AbstractNioReader;
import com.google.code.jstringserver.server.nio.select.AbstractNioWriter;
import com.google.code.jstringserver.server.nio.select.AbstractSelectionStrategy;
import com.google.code.jstringserver.server.nio.select.MultiThreadedSelectionStrategy;
import com.google.code.jstringserver.server.nio.select.NioReaderLooping;
import com.google.code.jstringserver.server.threads.NamedThreadFactory;
import com.google.code.jstringserver.server.wait.SleepWaitStrategy;

public class MultiThreadedSelectorStrategyTest extends SelectorStrategyTest {
    
    private static ExecutorService executorService;

    @BeforeClass
    public static void startExecutor() {
        int poolSize = Runtime.getRuntime().availableProcessors();
        executorService = new ThreadPoolExecutor(
            poolSize, 
            poolSize, 
            Long.MAX_VALUE, 
            TimeUnit.SECONDS, 
            new LinkedBlockingQueue<Runnable>(Integer.MAX_VALUE),
            new NamedThreadFactory(MultiThreadedSelectionStrategy.class.getSimpleName()));
    }
    
    @AfterClass
    public static void stopExecutor() {
        executorService.shutdownNow();
    }
    
    @Override
    protected AbstractSelectionStrategy createSelectionStrategy(AbstractNioWriter writer, AbstractNioReader reader) {
        return new MultiThreadedSelectionStrategy(null, null, writer, reader, executorService);
    }

    @Override
    protected AbstractNioReader createNioReader(ClientDataHandler clientDataHandler) {
        return new NioReaderLooping(clientDataHandler, getByteBufferStore(), 10000L, new SleepWaitStrategy(1));
    }
    
}
