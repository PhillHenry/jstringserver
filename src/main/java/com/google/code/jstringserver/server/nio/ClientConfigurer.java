package com.google.code.jstringserver.server.nio;

import static java.nio.channels.SelectionKey.OP_CONNECT;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class ClientConfigurer {

    private final Selector selector;

    public ClientConfigurer(
        Selector selector) {
            this.selector = selector;
    }

    public void register(SocketChannel socketChannel) throws IOException, ClosedChannelException {
        if (socketChannel != null) {
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, OP_READ | OP_CONNECT | OP_WRITE); 
        }
    }

}
