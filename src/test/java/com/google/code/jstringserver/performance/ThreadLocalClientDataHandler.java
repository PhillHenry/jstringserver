package com.google.code.jstringserver.performance;

import java.nio.ByteBuffer;

import com.google.code.jstringserver.server.handlers.ClientDataHandler;

class ThreadLocalClientDataHandler implements ClientDataHandler {
    
    private final ReturnMessage returnMessage = new ReturnMessage();

    private final String payload;

    private final TotalStats totalStats = new TotalStats();
    
    public ThreadLocalClientDataHandler(String payload) {
        this.payload = payload;
    }
    
    private final ThreadLocal<Integer> currentWrittenSize = new ThreadLocal<Integer>() {

        @Override
        protected Integer initialValue() {
            return 0;
        }
        
    };
    
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
    public int handleRead(ByteBuffer byteBuffer, Object key) {
        byte[] bytes = new byte[byteBuffer.limit()];
        int filled = totalStats.handleRead(byteBuffer, key, bytes);
        
        // thread locals
        currentBatchSize.set(currentBatchSize.get() +  byteBuffer.limit());
        receivedPayload.get().append(new String(bytes));
        return filled;
    }

    @Override
    public String end(Object key) {
        currentBatchSize.set(0);
        checkReceivedPayloadAndRest();
        
        totalStats.incrementNumOfCallsEnded();
        
        int writtenSoFar = currentWrittenSize.get();
        return returnMessage.messageToWriteNext(writtenSoFar);
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
    public boolean isReadingComplete(Object key) {
        return !(currentBatchSize.get() < payload.length());
    }
    
    @Override
    public int getNumEndCalls() {
        return totalStats.getNumEndCalls();
    }

    @Override
    public void handleWrite(int wrote, Object key) {
        currentWrittenSize.set(currentWrittenSize.get() + wrote);
    }

    @Override
    public boolean isWritingComplete(Object key) {
        int writtenSoFar = currentWrittenSize.get();
        return returnMessage.isWritingComplete(writtenSoFar);
    }
    
}