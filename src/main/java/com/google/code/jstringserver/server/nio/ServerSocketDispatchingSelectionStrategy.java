package com.google.code.jstringserver.server.nio;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import com.google.code.jstringserver.server.exchange.SocketChannelExchanger;
import com.google.code.jstringserver.server.nio.select.AbstractSelectionStrategy;
import com.google.code.jstringserver.server.wait.WaitStrategy;

public class ServerSocketDispatchingSelectionStrategy extends AbstractSelectionStrategy {

    private final SocketChannelExchanger socketChannelExchanger;

    public ServerSocketDispatchingSelectionStrategy(
        WaitStrategy            waitStrategy,
        Selector                serverSelector,
        SocketChannelExchanger  socketChannelExchanger) {
        super(waitStrategy, serverSelector);
        this.socketChannelExchanger = socketChannelExchanger;
    }

    @Override
    protected void handle(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        if (socketChannel != null) {
            this.socketChannelExchanger.ready(socketChannel);
        }
    }
}