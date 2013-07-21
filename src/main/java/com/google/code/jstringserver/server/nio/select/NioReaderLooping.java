package com.google.code.jstringserver.server.nio.select;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.google.code.jstringserver.server.bytebuffers.store.ByteBufferStore;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;
import com.google.code.jstringserver.server.wait.WaitStrategy;

public class NioReaderLooping implements AbstractNioReader {
    
    private final NioReader nioReader;
    private final WaitStrategy waitStrategy;
    private final long timeoutMs;

    public NioReaderLooping(
        ClientDataHandler clientDataHandler,
        ByteBufferStore byteBufferStore,
        long timeoutMs, 
        WaitStrategy waitStrategy) {
        this.nioReader = new NioReader(clientDataHandler, byteBufferStore);
        this.timeoutMs = timeoutMs;
        this.waitStrategy = waitStrategy;
    }

    @Override
    public int read(SelectionKey key, SocketChannel selectableChannel) throws IOException {
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
        return read;
    }

    protected long now() {
        return System.currentTimeMillis();
    }

}
