package com.google.code.jstringserver.server.nio;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import com.google.code.jstringserver.server.Server;
import com.google.code.jstringserver.server.wait.WaitStrategy;

public class AcceptorDispatcher implements Runnable {

    private final Selector                  serverSelector;
    private final SocketChannelExchanger    socketChannelExchanger;
    private final AbstractSelectionStrategy selectionStrategy;
    private final Server                    server;
    private volatile boolean                isRunning = true;

    public AcceptorDispatcher(Server server,
                          Selector serverSelector,
                          SocketChannelExchanger socketChannelExchanger,
                          WaitStrategy waitStrategy) {
        super();
        this.server = server;
        this.serverSelector = serverSelector;
        this.socketChannelExchanger = socketChannelExchanger;
        this.selectionStrategy = new AbstractSelectionStrategy(waitStrategy, serverSelector) {

            @Override
            protected void handle(SelectionKey key) throws IOException {
                ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                SocketChannel socketChannel = serverSocketChannel.accept();
                while (socketChannel != null) {
                    AcceptorDispatcher.this.socketChannelExchanger.ready(socketChannel);
                    socketChannel = serverSocketChannel.accept();
                }
            }
        };
    }

    @Override
    public void run() {
        try {
            server.register(serverSelector);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        while (isRunning) {
            accept();
        }
    }

    private void accept() {
        try {
            while (isRunning) {
                selectionStrategy.select();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() throws IOException {
        isRunning = false;
        serverSelector.close();
    }
}
