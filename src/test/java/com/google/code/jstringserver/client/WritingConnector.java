package com.google.code.jstringserver.client;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.google.code.jstringserver.server.bytebuffers.store.ByteBufferStore;

public class WritingConnector extends Connector {
    
    public static WritingConnector[] createWritingConnectors(int num, String address, int port, String payload, ByteBufferStore byteBufferStore) {
        WritingConnector[] connectors = new WritingConnector[num];
        for (int i = 0 ; i < connectors.length ; i++) {
            connectors[i] = new WritingConnector(address, port, payload, byteBufferStore);
        }
        return connectors;
    }

    private final String            payload;
    private final ByteBufferStore   byteBufferStore;

    public WritingConnector(String address, int port, String payload, ByteBufferStore byteBufferStore) {
        super(address, port);
        this.payload = payload;
        this.byteBufferStore = byteBufferStore;
    }

    @Override
    protected void connected(SocketChannel socketChannel) throws IOException {
        write(socketChannel);
        read(socketChannel);
    }

    private void read(SocketChannel socketChannel) throws IOException {
        ByteBuffer      byteBuffer      = byteBufferStore.getByteBuffer();
        byteBuffer.clear();
        int read = socketChannel.read(byteBuffer);
        byteBuffer.flip();
        byte[]          bytes           = new byte[byteBuffer.limit()];
        byteBuffer.get(bytes);
        //System.out.println("Client: " + new String(bytes) + " read " + read);
    }

    private void write(SocketChannel socketChannel) throws IOException {
        ByteBuffer      byteBuffer      = byteBufferStore.getByteBuffer();
        int             blockSize       = byteBuffer.capacity();
        byte[]          bytes           = payload.getBytes();
        byteBuffer.clear();
        for (int i = 0 ; i < payload.length() ; i += blockSize) {
            int end = i + blockSize;
            int length = end > payload.length() ? payload.length() - i : blockSize;
            byteBuffer.put(bytes, i, length);
            byteBuffer.flip();
            socketChannel.write(byteBuffer);
            byteBuffer.clear();
        }
    }
    
}
