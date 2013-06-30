package com.google.code.jstringserver.server.nio;

import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class BlockingSocketChannelExchanger implements SocketChannelExchanger {
    
    private final BlockingQueue<SocketChannel> blockingQueue;
    private volatile ReadyCallback readyCallback;

    public BlockingSocketChannelExchanger() {
        blockingQueue = new LinkedBlockingQueue<>();
    }

    @Override
    public void ready(SocketChannel socketChannel) {
        blockingQueue.add(socketChannel);
        if (readyCallback != null) {
            readyCallback.ready();
        }
    }

    @Override
    public SocketChannel consume() throws InterruptedException {
        return blockingQueue.take();
    }

    @Override
    public void setReadyCallback(ReadyCallback readyCallback) {
        this.readyCallback = readyCallback;
    }

}
