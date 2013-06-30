package com.google.code.jstringserver.performance;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.google.code.jstringserver.server.handlers.ClientDataHandler;

class TestingClientDataHandler implements ClientDataHandler {
    
    private final String payload;

    private final AtomicInteger numEndCalls = new AtomicInteger();
    
    private final AtomicLong numBytesData = new AtomicLong();
    
    public TestingClientDataHandler(String payload) {
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
    public void handle(ByteBuffer byteBuffer) {
        byte[] bytes = new byte[byteBuffer.limit()];
        byteBuffer.get(bytes);
        int filled = byteBuffer.limit(); //A buffer's limit is the index of the first element that should *not* be read or written - http://docs.oracle.com/javase/6/docs/api/java/nio/Buffer.html
        numBytesData.addAndGet(filled);
        currentBatchSize.set(currentBatchSize.get() +  filled);
        receivedPayload.get().append(new String(bytes));
    }

    @Override
    public String end() {
        currentBatchSize.set(0);
        checkReceivedPayloadAndRest();
        numEndCalls.incrementAndGet();
        
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
        return numEndCalls.get();
    }
    
}