package com.google.code.jstringserver.client;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.google.code.jstringserver.server.bytebuffers.store.ByteBufferStore;

public class ConstantWritingConnector extends WritingConnector {

    private static volatile boolean    isRunning        = true;
    private static final AtomicInteger totalCalls       = new AtomicInteger();
    private static final AtomicInteger totalErrors      = new AtomicInteger();
    private static final AtomicLong    totalCallingTime = new AtomicLong();

    public static void stop() {
        isRunning = false;
    }

    public static int getTotalCalls() {
        return totalCalls.get();
    }
    
    public static long getTotalCallTime() {
        return totalCallingTime.get();
    }
    
    public static int getTotalErrors() {
        return totalErrors.get();
    }

    public ConstantWritingConnector(
        String address,
        int port,
        String payload,
        ByteBufferStore byteBufferStore) {
        super(
            address,
            port,
            payload,
            byteBufferStore);
    }

    @Override
    protected void doCall() throws Exception {
        while (isRunning) {
            totalCalls.incrementAndGet();
            try {
                long start = System.currentTimeMillis();
                super.doCall();
                long duration = System.currentTimeMillis() - start;
                totalCallingTime.addAndGet(duration);
            } catch (Exception x) {
                x.printStackTrace();
                totalErrors.incrementAndGet();
            }
        }
    }

}
