package com.google.code.jstringserver.performance.main;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import com.google.code.jstringserver.server.bytebuffers.store.ByteBufferStore;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;
import com.google.code.jstringserver.server.nio.select.MultiThreadedReadingSelectionStrategy;
import com.google.code.jstringserver.server.nio.select.NioReaderLooping;
import com.google.code.jstringserver.server.nio.select.NioWriter;
import com.google.code.jstringserver.server.nio.select.SelectionStrategy;
import com.google.code.jstringserver.server.wait.SleepWaitStrategy;
import com.google.code.jstringserver.stats.Stopwatch;

public class MultiThreadedSelectorServerMain extends AbstractServerMain {

    public MultiThreadedSelectorServerMain() throws IOException {
        super();
    }

    public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
        MultiThreadedSelectorServerMain app = new MultiThreadedSelectorServerMain();
        app.start(args);
    }

    @Override
    protected SelectionStrategy createSelectionStrategy(ClientDataHandler clientDataHandler, ByteBufferStore byteBufferStore) {
        Stopwatch writerStopWatch = getStopWatchFor(MultiThreadedReadingSelectionStrategy.class.getSimpleName());
        Stopwatch readerStopWatch = getStopWatchFor(NioReaderLooping.class.getSimpleName());
        return new MultiThreadedReadingSelectionStrategy(
            null, 
            getClientSelector(), 
            new NioWriter(clientDataHandler, writerStopWatch), 
            new NioReaderLooping(clientDataHandler, byteBufferStore, 10000L, new SleepWaitStrategy(1), readerStopWatch ), 
            createThreadPool());
    }

    private ThreadPoolExecutor createThreadPool() {
        return new ThreadPoolExecutor(availableProcessors(), availableProcessors(), Long.MAX_VALUE, SECONDS, new LinkedBlockingQueue<Runnable>());
    }

}
