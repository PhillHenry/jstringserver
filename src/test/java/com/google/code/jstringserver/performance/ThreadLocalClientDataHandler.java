package com.google.code.jstringserver.performance;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.google.code.jstringserver.server.handlers.ClientDataHandler;

class ThreadLocalClientDataHandler implements ClientDataHandler {
    
    private final String payload;

    private final TotalStats totalStats = new TotalStats();
    
    public ThreadLocalClientDataHandler(String payload) {
        this.payload = payload;
    }
    
    private final ThreadLocal<Integer> currentBatchSize = new ThreadLocal<Integer>() {

        @Override
        protected Integer initialValue() {
            return 0;
        }
        
    };
    
    private final ThreadLocal<StringBuffer> receivedPayload = new ThreadLocal<StringBuffer>() {

        @Override
        protected StringBuffer initialValue() {
            return new StringBuffer();
        }
        
    };

    @Override
    public void handle(ByteBuffer byteBuffer, Object key) {
        byte[] bytes = totalStats.handle(byteBuffer, key);
        
        // thread locals
        currentBatchSize.set(currentBatchSize.get() +  byteBuffer.limit());
        receivedPayload.get().append(new String(bytes));
    }

    @Override
    public String end(Object key) {
        currentBatchSize.set(0);
        checkReceivedPayloadAndRest();
        
        totalStats.incrementNumOfCallsEnded();
        
        return "OK";
    }

    private void checkReceivedPayloadAndRest() {
        StringBuffer finalPayload = receivedPayload.get();
        String received = finalPayload.toString();
        if (!payload.equals(received)) {
            System.err.println("Didn't recieved expected payload\n" 
                    +   "Expected: <" + payload  + ">"
                    + "\nActual:   <" + received + ">");
        }
        finalPayload.setLength(0);
    }

    @Override
    public boolean ready() {
        //System.out.println("current size = " + currentBatchSize.get() + ", payload = " + payloadSize);
        return currentBatchSize.get() < payload.length();
    }
    
    @Override
    public int getNumEndCalls() {
        return totalStats.getNumEndCalls();
    }
    
}