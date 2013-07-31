package com.google.code.jstringserver.server.nio.select;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.google.code.jstringserver.server.bytebuffers.store.ByteBufferStore;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;
import com.google.code.jstringserver.server.wait.WaitStrategy;
import com.google.code.jstringserver.stats.Stopwatch;

public class NioReaderLooping implements AbstractNioReader {
    
    private final NioReader nioReader;
    private final WaitStrategy waitStrategy;
    private final long timeoutMs;
    private final Stopwatch stopwatch;

    public NioReaderLooping(
        ClientDataHandler clientDataHandler,
        ByteBufferStore byteBufferStore,
        long timeoutMs, 
        WaitStrategy waitStrategy, 
        Stopwatch stopwatch) {
        this.stopwatch = stopwatch;
        this.nioReader = new NioReader(clientDataHandler, byteBufferStore, null);
        this.timeoutMs = timeoutMs;
        this.waitStrategy = waitStrategy;
    }

    @Override
    public int read(SelectionKey key, SocketChannel selectableChannel) throws IOException {
        startTimer();
        try {
            return doRead(key, selectableChannel);
        } finally {
            stopTimer();
        }
    }

    private int doRead(SelectionKey key, SocketChannel selectableChannel) throws IOException {
        long start = now();
        int read = 0;
        do {
            if (now() - start > timeoutMs) {
                return -1;
            }
            read = nioReader.read(key, selectableChannel);
            if (read == 0) {
                if (!waitStrategy.pause()) {
                    return -1;
                }
            }
        } while (read != -1 && !nioReader.finished(key));
        if (read == -1) {
            close(key, selectableChannel);
        }
        return read;
    }

    private void stopTimer() {
        if (stopwatch != null) {
            stopwatch.stop();
        }
    }

    private void startTimer() {
        if (stopwatch != null) {
            stopwatch.start();
        }
    }

    private void close(SelectionKey key, SocketChannel selectableChannel) throws IOException {
        key.cancel();
        selectableChannel.close();
    }

    protected long now() {
        return System.currentTimeMillis();
    }

}
