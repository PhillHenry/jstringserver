package com.google.code.jstringserver.server.nio.select;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.google.code.jstringserver.server.bytebuffers.store.ByteBufferStore;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;
import com.google.code.jstringserver.stats.Stopwatch;

public class NioReader implements AbstractNioReader {
    private final ClientDataHandler clientDataHandler;
    private final ByteBufferStore   byteBufferStore;
    private final Stopwatch         stopwatch;

    public NioReader(
        ClientDataHandler clientDataHandler,
        ByteBufferStore byteBufferStore,
        Stopwatch stopwatch) {
        super();
        this.clientDataHandler = clientDataHandler;
        this.byteBufferStore = byteBufferStore;
        this.stopwatch = stopwatch;
    }

    @Override
    public int read(SelectionKey key, SocketChannel selectableChannel) throws IOException {
        start();
        try {
            ByteBuffer byteBuffer = byteBufferStore.getByteBuffer();
            byteBuffer.clear();
            int read = selectableChannel.read(byteBuffer);
            byteBuffer.flip();
            clientDataHandler.handle(byteBuffer, key);
            return read;
        } finally {
            stop();
        }
    }
    
    private void stop() {
        if (stopwatch != null) {
            stopwatch.stop();
        }
    }

    private void start() {
        if (stopwatch != null) {
            stopwatch.start();
        }
    }

    protected boolean finished(SelectionKey key) {
        return !clientDataHandler.isNotComplete(key);
    }
}
