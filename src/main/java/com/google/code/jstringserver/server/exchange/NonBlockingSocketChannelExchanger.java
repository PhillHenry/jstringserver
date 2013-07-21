package com.google.code.jstringserver.server.exchange;

import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NonBlockingSocketChannelExchanger extends AbstractSocketChannelExchanger {
    
    private final Queue<SocketChannel> queue;

    public NonBlockingSocketChannelExchanger() {
        queue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public SocketChannel consume() throws InterruptedException {
        return queue.poll();
    }

    @Override
    protected void add(SocketChannel socketChannel) {
        queue.offer(socketChannel);
    }

}
