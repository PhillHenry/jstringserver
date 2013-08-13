package com.google.code.jstringserver.client;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;

import com.google.code.jstringserver.server.bytebuffers.store.ByteBufferStore;

public class LatchedWritingConnector extends WritingConnector {

    public static LatchedWritingConnector[] createWritingConnectors(
        int num,
        String address,
        int port,
        String payload,
        ByteBufferStore byteBufferStore,
        CountDownLatch preWrite,
        CountDownLatch postWrite,
        CountDownLatch preRead,
        CountDownLatch postRead,
        CountDownLatch postClose) {
        LatchedWritingConnector[] connectors = new LatchedWritingConnector[num];
        for (int i = 0; i < connectors.length; i++) {
            connectors[i] = new LatchedWritingConnector(address, port, payload, byteBufferStore, preWrite, postWrite, preRead, postRead, postClose);
        }
        return connectors;
    }

    private final CountDownLatch preWrite;
    private final CountDownLatch postWrite;
    private final CountDownLatch postRead;
    private final CountDownLatch preRead;
    private CountDownLatch postClose;

    public LatchedWritingConnector(
        String address,
        int port,
        String payload,
        ByteBufferStore byteBufferStore,
        CountDownLatch preWrite,
        CountDownLatch postWrite,
        CountDownLatch preRead,
        CountDownLatch postRead,
        CountDownLatch postClose) {
        super(address, port, payload, byteBufferStore, null, null, null);
        this.preWrite = preWrite;
        this.postWrite = postWrite;
        this.preRead = preRead;
        this.postRead = postRead;
        this.postClose = postClose;
    }

    @Override
    protected int read(SocketChannel socketChannel) throws IOException {
        preRead.countDown();
        int read = super.read(socketChannel);
        postRead.countDown();
        return read;
    }

    @Override
    protected void write(SocketChannel socketChannel) throws IOException {
        preWrite.countDown();
        super.write(socketChannel);
        postWrite.countDown();
    }

    @Override
    protected void close(SocketChannel socketChannel) throws IOException {
        socketChannel.configureBlocking(true);
        super.close(socketChannel);
        postClose.countDown();
    }

}
