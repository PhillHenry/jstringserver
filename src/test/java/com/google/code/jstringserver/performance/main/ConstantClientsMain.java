package com.google.code.jstringserver.performance.main;

import static com.google.code.jstringserver.client.ConstantWritingConnector.getTotalCallTime;
import static com.google.code.jstringserver.client.ConstantWritingConnector.getTotalCalls;
import static com.google.code.jstringserver.client.ConstantWritingConnector.getTotalErrors;
import static com.google.code.jstringserver.performance.main.AbstractServerMain.EXPECTED_PAYLOAD;
import static com.google.code.jstringserver.performance.main.AbstractServerMain.PORT;
import static com.google.code.jstringserver.stats.HdrHistogramStats.MAX_READING;
import static com.google.code.jstringserver.stats.HistogramTimer.alwaysHistogramTimer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.code.jstringserver.client.ConstantWritingConnector;
import com.google.code.jstringserver.server.bytebuffers.factories.DirectByteBufferFactory;
import com.google.code.jstringserver.server.bytebuffers.store.ThreadLocalByteBufferStore;
import com.google.code.jstringserver.stats.CsvLinearHistogramFormatStrategy;
import com.google.code.jstringserver.stats.HdrHistogramStats;
import com.google.code.jstringserver.stats.HistogramTimer;
import com.google.code.jstringserver.stats.LinearHistogramFormatStrategy;
import com.google.code.jstringserver.stats.Stats;
import com.google.code.jstringserver.stats.Stopwatch;
import com.google.code.jstringserver.stats.SynchronizedStatsDecorator;
import com.google.code.jstringserver.stats.ThreadLocalStats;
import com.google.code.jstringserver.stats.ThreadLocalStopWatch;

public class ConstantClientsMain {

    private ThreadLocalStopWatch readStopWatch;
    private ThreadLocalStopWatch writeStopWatch;
    private ThreadLocalStopWatch connectStopWatch;
    private ThreadLocalStopWatch totalStopWatch;

    public static void main(
            String[] args) throws InterruptedException {
        ConstantClientsMain app = new ConstantClientsMain();
        app.start(args);
    }
    
    public void start(String[] args) throws InterruptedException {
        run(args);
        doMetrics(readStopWatch, writeStopWatch, connectStopWatch, totalStopWatch);
    }

    public ConstantWritingConnector[] run(String[] args) throws InterruptedException {
        String address                              = getAddress(args);
        int numThreads                              = getNumberOfThreads(args);
        ThreadLocalByteBufferStore byteBufferStore  = createByteBufferStore();
        readStopWatch                               = new ThreadLocalStopWatch("read", newStats());
        writeStopWatch                              = new ThreadLocalStopWatch("write", newStats());
        connectStopWatch                            = new ThreadLocalStopWatch("connect", newStats());
        totalStopWatch                            	= new ThreadLocalStopWatch("total", newStats());
        ConstantWritingConnector[] connectors       = createConnectors(
            address,
            numThreads,
            numThreads,
            byteBufferStore, 
            readStopWatch, 
            writeStopWatch, 
            connectStopWatch, 
            totalStopWatch);
        run(numThreads,
            connectors);
        return connectors;
    }

    private Stats newStats() {
//        return new ThreadLocalStats(sampleSizeHint);
        CsvLinearHistogramFormatStrategy formatter = new CsvLinearHistogramFormatStrategy(30);
		return new SynchronizedStatsDecorator(
        		new HdrHistogramStats(alwaysHistogramTimer, formatter));
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

    private static void doMetrics(Stopwatch readStopWatch, Stopwatch writeStopWatch, Stopwatch connectStopWatch, Stopwatch totalStopWatch) throws InterruptedException {
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
            System.out.println(totalStopWatch);
            System.out.println();
            
            Thread.sleep(sleepTime);
            oldTotalCalls = totalCalls;
        }
    }

    private static void run(
        int numThreads,
        ConstantWritingConnector[] connectors) throws InterruptedException {
        ThreadPoolExecutor          threadPool = createThreadpool(numThreads);
        for (Runnable command : connectors) {
            threadPool.execute(command);
            Thread.sleep(10);
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
            Stopwatch connectTimer, 
            Stopwatch totalStopWatch) {
        ConstantWritingConnector[] constantWritingConnectors    = new ConstantWritingConnector[num];
        for (int i = 0; i < numThreads; i++) {
            constantWritingConnectors[i] = new ConstantWritingConnector(
                    address, 
                    PORT,
                    EXPECTED_PAYLOAD,
                    byteBufferStore, 
                    readStopWatch, 
                    writeStopWatch, 
                    connectTimer,
                    totalStopWatch);
        }
        return constantWritingConnectors;
    }

    private static ThreadLocalByteBufferStore createByteBufferStore() {
        return new ThreadLocalByteBufferStore(new DirectByteBufferFactory(1024));
    }

    public ThreadLocalStopWatch getReadStopWatch() {
        return readStopWatch;
    }

    public ThreadLocalStopWatch getWriteStopWatch() {
        return writeStopWatch;
    }

    public ThreadLocalStopWatch getConnectStopWatch() {
        return connectStopWatch;
    }

}
