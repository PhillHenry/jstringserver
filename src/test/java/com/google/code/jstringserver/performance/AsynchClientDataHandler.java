package com.google.code.jstringserver.performance;

import static java.lang.System.currentTimeMillis;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.code.jstringserver.server.handlers.ClientDataHandler;

public class AsynchClientDataHandler implements ClientDataHandler {
    
    private final ReturnMessage returnMessage = new ReturnMessage();

    private final String payload;
    
    private final TotalStats totalStats = new TotalStats();

    private final long timeoutMs;
    
    class CurrentStats {
        final AtomicInteger bytesRead = new AtomicInteger();
        final AtomicInteger bytesWritten = new AtomicInteger();
        final StringBuffer read = new StringBuffer();
        final long createdTimeMs = currentTimeMillis();
        
        boolean isTimedOut() {
            boolean timeout = createdTimeMs < (currentTimeMillis() - timeoutMs);
            return timeout;
        }

        @Override
        public String toString() {
            return "CurrentStats [bytesRead="
                + bytesRead
                + ", bytesWritten="
                + bytesWritten
//                + ", read="
//                + read
//                + ", createdTimeMs="
//                + createdTimeMs
                + ", Total time = " + (currentTimeMillis() - createdTimeMs) + "ms"
                + "]";
        }
    }

    public AsynchClientDataHandler(
        String payload) {
        this(payload, 30000L);
    }

    public AsynchClientDataHandler(String payload, long timeoutMs) {
        super();
        this.payload = payload;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public int handleRead(ByteBuffer byteBuffer, Object key) {
        CurrentStats    attachment  = getCurrentStats(key);
        byte[]          bytes       = new byte[byteBuffer.limit()];
        int             filled      = totalStats.handleRead(byteBuffer, key, bytes);
        if (filled > 0) {
            updateCurrentStats(attachment, bytes);
        }
        return filled;
    }

    private void updateCurrentStats(CurrentStats attachment, byte[] bytes) {
        attachment.read.append(new String(bytes));
        attachment.bytesRead.addAndGet(bytes.length);
        if (receivedAll(attachment)) {
            totalStats.incrementNumOfCallsEnded();
        }
    }

    private boolean receivedAll(CurrentStats attachment) {
        return attachment != null && attachment.bytesRead.get() == payload.length();
    }

    @Override
    public byte[] end(Object key) {
        if (receivedAll(getCurrentStats(key))) {
            int writtenSoFar = getBytesWrittenSoFar(key);
            return returnMessage.messageToWriteNext(writtenSoFar);
        }
        return null;
    }

    @Override
    public boolean isReadingComplete(Object key) {
        CurrentStats currentStats = getCurrentStats(key);
        return receivedAll(currentStats) || isTimedOut(currentStats);
    }

    private boolean isTimedOut(CurrentStats currentStats) {
        return currentStats != null && currentStats.isTimedOut();
    }

    @Override
    public int getNumEndCalls() {
        return totalStats.getNumEndCalls();
    }

    @Override
    public void handleWrite(int wrote, Object key) {
        CurrentStats attachment = getCurrentStats(key);
        attachment.bytesWritten.addAndGet(wrote);
    }

    private CurrentStats getCurrentStats(Object key) {
        if (key instanceof SelectionKey) {
            SelectionKey selectionKey = (SelectionKey)key;
            CurrentStats attachment = getAttachment(selectionKey);
            return attachment;
        } else if (key instanceof CurrentStats) {
            return (CurrentStats)key;
        } else {
            throw new RuntimeException("" + key);
        }
    }


    private CurrentStats getAttachment(SelectionKey selectionKey) {
        CurrentStats attachment = (CurrentStats) selectionKey.attachment();
        if (attachment == null) {
            attachment = new CurrentStats();
            selectionKey.attach(attachment);
        }
        return attachment;
    }
    
    @Override
    public boolean isWritingComplete(Object key) {
        int writtenSoFar = getBytesWrittenSoFar(key);
        return returnMessage.isWritingComplete(writtenSoFar);
    }

    private int getBytesWrittenSoFar(Object key) {
        CurrentStats attachment = getCurrentStats(key);
        int writtenSoFar = attachment.bytesWritten.get();
        return writtenSoFar;
    }

    @Override
    public boolean isTimedOut(Object key) {
        CurrentStats attachment = getCurrentStats(key);
        boolean timedOut = attachment.isTimedOut();
        // TEST
        if (timedOut) {
            System.out.println("Timedout: " + attachment);
        }
        return timedOut;
    }

    @Override
    public Object getKey() {
        return new CurrentStats();
    }

}
