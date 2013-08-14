package com.google.code.jstringserver.performance.main;

import static com.google.code.jstringserver.performance.main.AbstractServerMain.EXPECTED_PAYLOAD;
import static com.google.code.jstringserver.performance.main.AbstractServerMain.getConnectedServer;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.channels.Selector;

import com.google.code.jstringserver.performance.AsynchClientDataHandler;
import com.google.code.jstringserver.server.BatchAcceptorAndReadingThreadStrategy;
import com.google.code.jstringserver.server.Server;
import com.google.code.jstringserver.server.ThreadPoolFactory;
import com.google.code.jstringserver.server.bytebuffers.factories.DirectByteBufferFactory;
import com.google.code.jstringserver.server.bytebuffers.store.ByteBufferStore;
import com.google.code.jstringserver.server.bytebuffers.store.ThreadLocalByteBufferStore;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;
import com.google.code.jstringserver.server.nio.select.AbstractNioReader;
import com.google.code.jstringserver.server.nio.select.AbstractNioWriter;
import com.google.code.jstringserver.server.nio.select.BatchServerAndReadingSelectionStrategy;
import com.google.code.jstringserver.server.nio.select.NioReader;
import com.google.code.jstringserver.server.nio.select.NioReaderLooping;
import com.google.code.jstringserver.server.nio.select.NioWriter;
import com.google.code.jstringserver.server.wait.NoOpWaitStrategy;
import com.google.code.jstringserver.server.wait.WaitStrategy;
import com.google.code.jstringserver.stats.Stopwatch;

public class BatchedServerMain {

    private final StatsCollector statsCollector;
    private Server server;
    private BatchAcceptorAndReadingThreadStrategy threadStrategy;

    public BatchedServerMain() throws IOException {
        super();
        statsCollector                          = new StatsCollector();
    }

    private void startServerAndCollectData(String[] args) throws Exception {
        startServer(args);
        statsCollector.started();
    }

    public void startServer(String[] args) throws UnknownHostException, IOException, Exception {
        String              ipInterface         = args.length < 1 ? "localhost" : args[0];
        server                                  = getConnectedServer(ipInterface);

        ClientDataHandler   clientDataHandler   = new AsynchClientDataHandler(EXPECTED_PAYLOAD);
        AbstractNioReader   reader              = createReader(clientDataHandler);
        AbstractNioWriter   writer              = createWriter(clientDataHandler);
        
        ThreadPoolFactory   threadPoolFactory   = new ThreadPoolFactory(2);
        Stopwatch           stopwatch           = statsCollector.getStopWatchFor(BatchServerAndReadingSelectionStrategy.class.getSimpleName());
        threadStrategy                          = new BatchAcceptorAndReadingThreadStrategy(
            server, 
            reader, 
            threadPoolFactory, 
            writer,
            stopwatch);
        threadStrategy.start();
    }
    
    public void stop() throws IOException {
        server.shutdown();
        threadStrategy.shutdown();
    }

    private AbstractNioWriter createWriter(ClientDataHandler clientDataHandler) {
        Stopwatch stopwatch = statsCollector.getStopWatchFor(NioWriter.class.getSimpleName());;
        return new NioWriter(clientDataHandler, stopwatch);
    }

    protected AbstractNioReader createReader(ClientDataHandler clientDataHandler) {
        Stopwatch           stopwatch           = statsCollector.getStopWatchFor(NioReader.class.getSimpleName());
        ByteBufferStore     byteBufferStore     = createByteBufferStore();
        return new NioReader(
            clientDataHandler, 
            byteBufferStore, 
            stopwatch);
    }

    protected ByteBufferStore createByteBufferStore() {
        return new ThreadLocalByteBufferStore(new DirectByteBufferFactory(4096));
    }

    protected WaitStrategy createWaitStrategy() {
        return new NoOpWaitStrategy();
    }

    public static void main(String[] args) throws Exception {
        BatchedServerMain app = new BatchedServerMain();
        app.startServerAndCollectData(args);
    }

}
