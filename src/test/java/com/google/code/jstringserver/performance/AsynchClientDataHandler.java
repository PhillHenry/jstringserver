package com.google.code.jstringserver.performance;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.code.jstringserver.server.handlers.ClientDataHandler;

public class AsynchClientDataHandler implements ClientDataHandler {
    
    private final String payload;
    
    private final TotalStats totalStats = new TotalStats();
    
    class CurrentStats {
        final AtomicInteger bytesRead = new AtomicInteger();
        final StringBuffer read = new StringBuffer();
    }

    public AsynchClientDataHandler(String payload) {
        super();
        this.payload = payload;
    }

    @Override
    public int handleRead(ByteBuffer byteBuffer, Object key) {
        SelectionKey selectionKey = (SelectionKey)key;
        CurrentStats attachment = getAttachment(selectionKey);
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
    public String end(Object key) {
        SelectionKey selectionKey = (SelectionKey)key;
        if (receivedAll((CurrentStats)selectionKey.attachment())) {
            return "OK";
        }
        return null;
    }

    @Override
    public boolean isNotComplete(Object key) {
        SelectionKey selectionKey = (SelectionKey)key;
        return !receivedAll((CurrentStats)selectionKey.attachment());
    }

    @Override
    public int getNumEndCalls() {
        return totalStats.getNumEndCalls();
    }

}
