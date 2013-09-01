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
        try {
            int read = super.read(socketChannel);
            return read;
        } finally {
            postRead.countDown();
        }
    }

    @Override
    protected void write(SocketChannel socketChannel) throws IOException {
        preWrite.countDown();
        try {
            super.write(socketChannel);
        } finally {
            postWrite.countDown();
        }
    }

    @Override
    protected void close(SocketChannel socketChannel) throws IOException {
        try {
            try {
            /*
             * This failing can leave a CLOSE_WAIT:
netstat -na | grep 50038
tcp4      19      0  127.0.0.1.50038        127.0.0.1.50039        CLOSE_WAIT 
tcp4       0      0  127.0.0.1.50038        *.*                    LISTEN  
             */
                socketChannel.configureBlocking(true);
            } catch (Exception x) {
                System.err.println("Failed to put client in non-blocking mode. This may effect your test. Error = " + x.getMessage());
            }
            super.close(socketChannel);
        } finally {
            postClose.countDown();
        }
    }

}
