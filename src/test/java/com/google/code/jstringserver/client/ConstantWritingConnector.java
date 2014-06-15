package com.google.code.jstringserver.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.google.code.jstringserver.server.bytebuffers.store.ByteBufferStore;
import com.google.code.jstringserver.stats.Stopwatch;

public class ConstantWritingConnector extends WritingConnector {

    private static volatile boolean    isRunning        = true;
    private static final AtomicInteger totalCalls       = new AtomicInteger();
    private static final AtomicInteger totalErrors      = new AtomicInteger();
    private static final AtomicLong    totalCallingTime = new AtomicLong();
    private static final AtomicInteger bytesInPayload   = new AtomicInteger();
    
    private final int payloadSize;
    private byte[] byteArray;
	private final Stopwatch totalStopWatch;

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
        ByteBufferStore byteBufferStore,
        Stopwatch readStopWatch, 
        Stopwatch writeStopWatch, 
        Stopwatch connectTimer, 
        Stopwatch totalStopWatch) {
        super(
            address,
            port,
            payload,
            byteBufferStore, 
            readStopWatch, 
            writeStopWatch, 
            connectTimer);
		this.totalStopWatch = totalStopWatch;
        payloadSize = payload.length();
    }

    @Override
    protected void doCall() throws Exception {
        while (isRunning) {
            totalCalls.incrementAndGet();
            try {
                long start = System.currentTimeMillis();
                totalStopWatch.start();
                super.doCall();
                totalStopWatch.stop();
                long duration = System.currentTimeMillis() - start;
                totalCallingTime.addAndGet(duration);
            } catch (Exception x) {
                x.printStackTrace();
                totalErrors.incrementAndGet();
            }
        }
    }

    @Override
    protected int read(SocketChannel socketChannel) throws IOException {
//        int read = -1;
//        while ((read = super.read(socketChannel)) != -1 && bytesInPayload.get() < payloadSize) {
//            bytesInPayload.addAndGet(read);
//        }
        int read = super.read(socketChannel);
        bytesInPayload.addAndGet(read);
        return read;
    }
    
    protected byte[] getByteArray(ByteBuffer byteBuffer) {
        if (byteArray == null) {
            byteArray = super.getByteArray(byteBuffer);
        }
        return byteArray;
    }

    @Override
    protected void connected(SocketChannel socketChannel) throws IOException {
        bytesInPayload.set(0);
        
//        socketChannel.socket().setSoTimeout(10000);
//        socketChannel.socket().setReuseAddress(true);
//        socketChannel.socket().setSoLinger(false, 10000);
        socketChannel.socket().setTcpNoDelay(false);
        super.connected(socketChannel);
    }

}
