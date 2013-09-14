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
        SelectionKey selectionKey = (SelectionKey)key;
        CurrentStats attachment = getCurrentStats(key);
        byte[] bytes = new byte[byteBuffer.limit()];
        int filled = totalStats.handleRead(byteBuffer, selectionKey, bytes);
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

    private CurrentStats getAttachment(SelectionKey selectionKey) {
        CurrentStats attachment = (CurrentStats) selectionKey.attachment();
        if (attachment == null) {
            attachment = new CurrentStats();
            selectionKey.attach(attachment);
        }
        return attachment;
    }

    @Override
    public byte[] end(Object key) {
        SelectionKey selectionKey = (SelectionKey)key;
        if (receivedAll((CurrentStats)selectionKey.attachment())) {
            int writtenSoFar = getBytesWrittenSoFar(key);
            return returnMessage.messageToWriteNext(writtenSoFar);
        }
        return null;
    }
    


    @Override
    public boolean isReadingComplete(Object key) {
        SelectionKey selectionKey = (SelectionKey)key;
        CurrentStats currentStats = (CurrentStats)selectionKey.attachment();
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
        SelectionKey selectionKey = (SelectionKey)key;
        CurrentStats attachment = getAttachment(selectionKey);
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

}
