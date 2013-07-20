package com.google.code.jstringserver.performance.main;

import static com.google.code.jstringserver.client.ConstantWritingConnector.getTotalCallTime;
import static com.google.code.jstringserver.client.ConstantWritingConnector.getTotalCalls;
import static com.google.code.jstringserver.client.ConstantWritingConnector.getTotalErrors;
import static com.google.code.jstringserver.performance.main.SelectorServerMain.EXPECTED_PAYLOAD;
import static com.google.code.jstringserver.performance.main.SelectorServerMain.PORT;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.code.jstringserver.client.ConstantWritingConnector;
import com.google.code.jstringserver.server.bytebuffers.factories.DirectByteBufferFactory;
import com.google.code.jstringserver.server.bytebuffers.store.ThreadLocalByteBufferStore;

public class ConstantClientsMain {

    public static void main(
            String[] args) throws InterruptedException {
        String address                              = getAddress(args);
        int numThreads                              = getNumberOfThreads(args);
        ThreadLocalByteBufferStore byteBufferStore  = createByteBufferStore();
        ConstantWritingConnector[] connectors       = createConnectors(
            address,
            numThreads,
            numThreads,
            byteBufferStore);
        run(numThreads,
            connectors);
        doMetrics();
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

    private static void doMetrics() throws InterruptedException {
        int oldTotalCalls = 0;
        int sleepTime = 1000;
        while (true) {
            int totalCalls = getTotalCalls();
            System.out.println("Initiated " + totalCalls + 
                " calls. Calls per second = " + ((totalCalls - oldTotalCalls) * 1000) / sleepTime +
                ". number of errors at client side = " + getTotalErrors() + 
                ". Average call time = " + getTotalCallTime() / totalCalls + "ms");
            
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
            ThreadLocalByteBufferStore byteBufferStore) {
        ConstantWritingConnector[] constantWritingConnectors    = new ConstantWritingConnector[num];
        for (int i = 0; i < numThreads; i++) {
            constantWritingConnectors[i] = new ConstantWritingConnector(
                    address, 
                    PORT,
                    EXPECTED_PAYLOAD,
                    byteBufferStore);
        }
        return constantWritingConnectors;
    }

    private static ThreadLocalByteBufferStore createByteBufferStore() {
        return new ThreadLocalByteBufferStore(new DirectByteBufferFactory(1024));
    }

}
