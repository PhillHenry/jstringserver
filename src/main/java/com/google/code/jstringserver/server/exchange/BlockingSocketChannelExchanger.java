package com.google.code.jstringserver.server.exchange;

import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class BlockingSocketChannelExchanger extends AbstractSocketChannelExchanger {
    
    private final BlockingQueue<SocketChannel> blockingQueue;

    public BlockingSocketChannelExchanger() {
        blockingQueue = new LinkedBlockingQueue<>();
    }

    @Override
    public SocketChannel consume() throws InterruptedException {
        return blockingQueue.poll();
    }

    @Override
    protected void add(SocketChannel socketChannel) {
        blockingQueue.add(socketChannel);
    }

}
