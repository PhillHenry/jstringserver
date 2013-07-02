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
    public void handle(ByteBuffer byteBuffer, Object key) {
        SelectionKey selectionKey = (SelectionKey)key;
        CurrentStats attachment = getAttachment(selectionKey);
        byte[] bytes = totalStats.handle(byteBuffer, selectionKey);
        updateCurrentStats(attachment, bytes);
    }

    private void updateCurrentStats(CurrentStats attachment, byte[] bytes) {
        attachment.read.append(new String(bytes));
        attachment.bytesRead.addAndGet(bytes.length);
        if (attachment.bytesRead.get() == payload.length()) {
            totalStats.incrementNumOfCallsEnded();
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
    public String end() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public boolean ready() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public int getNumEndCalls() {
        return totalStats.getNumEndCalls();
    }

}
