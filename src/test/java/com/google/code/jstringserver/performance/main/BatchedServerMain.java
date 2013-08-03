package com.google.code.jstringserver.performance.main;

import static com.google.code.jstringserver.performance.main.AbstractServerMain.EXPECTED_PAYLOAD;
import static com.google.code.jstringserver.performance.main.AbstractServerMain.getConnectedServer;

import java.io.IOException;
import java.nio.channels.Selector;

import com.google.code.jstringserver.performance.AsynchClientDataHandler;
import com.google.code.jstringserver.server.Server;
import com.google.code.jstringserver.server.bytebuffers.factories.DirectByteBufferFactory;
import com.google.code.jstringserver.server.bytebuffers.store.ByteBufferStore;
import com.google.code.jstringserver.server.bytebuffers.store.ThreadLocalByteBufferStore;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;
import com.google.code.jstringserver.server.nio.select.AbstractNioReader;
import com.google.code.jstringserver.server.nio.select.AbstractNioWriter;
import com.google.code.jstringserver.server.nio.select.BatchServerAndReadingSelectionStrategy;
import com.google.code.jstringserver.server.nio.select.NioReaderLooping;
import com.google.code.jstringserver.server.nio.select.NioWriter;
import com.google.code.jstringserver.server.nio.select.SelectionStrategy;
import com.google.code.jstringserver.server.wait.NoOpWaitStrategy;
import com.google.code.jstringserver.server.wait.WaitStrategy;
import com.google.code.jstringserver.stats.Stopwatch;

public class BatchedServerMain {

    private final StatsCollector statsCollector;

    public BatchedServerMain() throws IOException {
        super();
        statsCollector                          = new StatsCollector();
    }

    private void startServer(String[] args) throws IOException, InterruptedException {
        String              ipInterface         = args.length < 1 ? "localhost" : args[0];
        Server              server              = getConnectedServer(ipInterface);
        
        Selector            selector            = Selector.open();
        server.register(selector);

        ClientDataHandler   clientDataHandler   = new AsynchClientDataHandler(EXPECTED_PAYLOAD);
        AbstractNioReader   reader              = createReader(clientDataHandler);
        AbstractNioWriter   writer              = createWriter(clientDataHandler);
        BatchServerAndReadingSelectionStrategy   strategy =
            new BatchServerAndReadingSelectionStrategy(createWaitStrategy(), selector, reader, writer);
        
        startListening(strategy);
        
        statsCollector.started();
    }

    private void startListening(final BatchServerAndReadingSelectionStrategy strategy) {
        Thread thread = new Thread(new Runnable() {
            
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    try {
                        strategy.select();
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        });
        thread.start();
    }

    private AbstractNioWriter createWriter(ClientDataHandler clientDataHandler) {
        Stopwatch stopwatch = statsCollector.getStopWatchFor(NioWriter.class.getSimpleName());;
        return new NioWriter(clientDataHandler, stopwatch);
    }

    protected NioReaderLooping createReader(ClientDataHandler clientDataHandler) {
        WaitStrategy        waitStrategy        = createWaitStrategy();
        Stopwatch           stopwatch           = statsCollector.getStopWatchFor(NioReaderLooping.class.getSimpleName());
        long                timeoutMs           = 2000L;
        ByteBufferStore     byteBufferStore     = createByteBufferStore();
        return new NioReaderLooping(clientDataHandler, byteBufferStore, timeoutMs, waitStrategy, stopwatch);
    }

    protected ByteBufferStore createByteBufferStore() {
        return new ThreadLocalByteBufferStore(new DirectByteBufferFactory(4096));
    }

    protected WaitStrategy createWaitStrategy() {
        return new NoOpWaitStrategy();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        BatchedServerMain app = new BatchedServerMain();
        app.startServer(args);
    }

}
