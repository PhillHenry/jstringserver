package com.google.code.jstringserver.performance.main;

import static com.google.code.jstringserver.client.ConstantWritingConnector.getTotalCallTime;
import static com.google.code.jstringserver.client.ConstantWritingConnector.getTotalCalls;
import static com.google.code.jstringserver.client.ConstantWritingConnector.getTotalErrors;
import static com.google.code.jstringserver.performance.main.AbstractServerMain.EXPECTED_PAYLOAD;
import static com.google.code.jstringserver.performance.main.AbstractServerMain.PORT;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.code.jstringserver.client.ConstantWritingConnector;
import com.google.code.jstringserver.server.bytebuffers.factories.DirectByteBufferFactory;
import com.google.code.jstringserver.server.bytebuffers.store.ThreadLocalByteBufferStore;
import com.google.code.jstringserver.stats.Stopwatch;
import com.google.code.jstringserver.stats.ThreadLocalStopWatch;

public class ConstantClientsMain {

    private static final int sampleSizeHint = 100;

    public static void main(
            String[] args) throws InterruptedException {
        String address                              = getAddress(args);
        int numThreads                              = getNumberOfThreads(args);
        ThreadLocalByteBufferStore byteBufferStore  = createByteBufferStore();
        Stopwatch                  readStopWatch    = new ThreadLocalStopWatch("read", sampleSizeHint);
        Stopwatch                  writeStopWatch   = new ThreadLocalStopWatch("write", sampleSizeHint);
        Stopwatch                  connectStopWatch = new ThreadLocalStopWatch("connect", sampleSizeHint);
        ConstantWritingConnector[] connectors       = createConnectors(
            address,
            numThreads,
            numThreads,
            byteBufferStore, 
            readStopWatch, 
            writeStopWatch, 
            connectStopWatch);
        run(numThreads,
            connectors);
        doMetrics(readStopWatch, writeStopWatch, connectStopWatch);
    }

    private static int getNumberOfThreads(String[] args) {
        int numThreads = args.length < 1
            ? Runtime.getRuntime().availableProcessors()
            : Integer.parseInt(args[0]);
        System.out.println("Number of threads = " + numThreads);
        return numThreads;
    }

    private static String getAddress(String[] args) {
        String host = args.length < 2 ? "localhost" : args[1];
        System.out.println("Host = " + host);
        return host;
    }

    private static void doMetrics(Stopwatch readStopWatch, Stopwatch writeStopWatch, Stopwatch connectStopWatch) throws InterruptedException {
        int oldTotalCalls = 0;
        int sleepTime = 2000;
        while (true) {
            int totalCalls = getTotalCalls();
            System.out.println("Initiated " + totalCalls + 
                " calls. Calls per second = " + ((totalCalls - oldTotalCalls) * 1000) / sleepTime +
                ". number of errors at client side = " + getTotalErrors() + 
                ". Average call time = " + (totalCalls != 0 ? ("" + getTotalCallTime() / totalCalls) : "NA") + "ms");
            System.out.println(readStopWatch);
            System.out.println(writeStopWatch);
            System.out.println(connectStopWatch);
            System.out.println();
            
            Thread.sleep(sleepTime);
            oldTotalCalls = totalCalls;
        }
    }

    private static void run(
        int numThreads,
        ConstantWritingConnector[] connectors) {
        ThreadPoolExecutor          threadPool = createThreadpool(numThreads);
        for (Runnable command : connectors) {
            threadPool.execute(command);
        }
    }

    private static ThreadPoolExecutor createThreadpool(
        int numThreads) {
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
            numThreads,
            numThreads,
            1000L,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(numThreads));
        return threadPool;
    }

    private static ConstantWritingConnector[] createConnectors(
            String address,
            int numThreads,
            int num,
            ThreadLocalByteBufferStore byteBufferStore,
            Stopwatch readStopWatch, 
            Stopwatch writeStopWatch, 
            Stopwatch connectTimer) {
        ConstantWritingConnector[] constantWritingConnectors    = new ConstantWritingConnector[num];
        for (int i = 0; i < numThreads; i++) {
            constantWritingConnectors[i] = new ConstantWritingConnector(
                    address, 
                    PORT,
                    EXPECTED_PAYLOAD,
                    byteBufferStore, 
                    readStopWatch, 
                    writeStopWatch, 
                    connectTimer);
        }
        return constantWritingConnectors;
    }

    private static ThreadLocalByteBufferStore createByteBufferStore() {
        return new ThreadLocalByteBufferStore(new DirectByteBufferFactory(1024));
    }

}
