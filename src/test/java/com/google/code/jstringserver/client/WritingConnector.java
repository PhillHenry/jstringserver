package com.google.code.jstringserver.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.google.code.jstringserver.server.bytebuffers.store.ByteBufferStore;
import com.google.code.jstringserver.stats.Stopwatch;

public class WritingConnector extends Connector {

    public static WritingConnector[] createWritingConnectors(
        int num,
        String address,
        int port,
        String payload,
        ByteBufferStore byteBufferStore) {
        WritingConnector[] connectors = new WritingConnector[num];
        for (int i = 0; i < connectors.length; i++) {
            connectors[i] = new WritingConnector(address, port, payload, byteBufferStore, null, null, null);
        }
        return connectors;
    }

    private final String          payload;
    private final ByteBufferStore byteBufferStore;
    private final Stopwatch       readTimer;
    private final Stopwatch       writeTimer;

    public WritingConnector(
        String address,
        int port,
        String payload,
        ByteBufferStore byteBufferStore,
        Stopwatch readTimer,
        Stopwatch writeTimer, 
        Stopwatch connectTimer) {
        super(address, port, connectTimer);
        this.payload = payload;
        this.byteBufferStore = byteBufferStore;
        this.readTimer = readTimer;
        this.writeTimer = writeTimer;
    }

    @Override
    protected void connected(SocketChannel socketChannel) throws IOException {
        startWrite();
        try {
            write(socketChannel);
        } finally {
            stopWrite();
        }
        startRead();
        try {
            read(socketChannel);
        } finally {
            stopRead();
        }
    }
    
    private void stopRead() {
        if (readTimer != null) {
            readTimer.stop();
        }
    }
    
    private void stopWrite() {
        if (writeTimer != null) {
            writeTimer.stop();
        }
    }
    
    private void startRead() {
        if (readTimer != null) {
            readTimer.start();
        }
    }
    
    private void startWrite() {
        if (writeTimer != null) {
            writeTimer.start();
        }
    }

    protected int read(SocketChannel socketChannel) throws IOException {
        ByteBuffer byteBuffer = byteBufferStore.getByteBuffer();
        byteBuffer.clear();
        int read = socketChannel.read(byteBuffer);
        byteBuffer.flip();
        byte[] bytes = new byte[byteBuffer.limit()];
        byteBuffer.get(bytes);
        // System.out.println("Client: " + new String(bytes) + " read " + read);
        return read;
    }

    protected void write(SocketChannel socketChannel) throws IOException {
        ByteBuffer byteBuffer = byteBufferStore.getByteBuffer();
        int blockSize = byteBuffer.capacity();
        byte[] bytes = payload.getBytes();
        byteBuffer.clear();
        for (int i = 0; i < payload.length(); i += blockSize) {
            int end = i + blockSize;
            int length = end > payload.length() ? payload.length() - i : blockSize;
            byteBuffer.put(bytes, i, length);
            byteBuffer.flip();
            socketChannel.write(byteBuffer);
            byteBuffer.clear();
        }
    }

}
