package com.google.code.jstringserver.performance;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TotalStats {
    private final AtomicInteger numEndCalls = new AtomicInteger();
    
    private final AtomicLong numBytesData = new AtomicLong();
    
    public byte[] handle(ByteBuffer byteBuffer, Object key) {
        byte[] bytes = new byte[byteBuffer.limit()];
        byteBuffer.get(bytes);
        int filled = byteBuffer.limit(); //A buffer's limit is the index of the first element that should *not* be read or written - http://docs.oracle.com/javase/6/docs/api/java/nio/Buffer.html
        numBytesData.addAndGet(filled);
        return bytes;
    }
    
    public int getNumEndCalls() {
        return numEndCalls.get();
    }

    public void incrementNumOfCallsEnded() {
        numEndCalls.incrementAndGet();        
    }
}
