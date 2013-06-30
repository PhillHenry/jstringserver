package com.google.code.jstringserver.server.nio;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

import com.google.code.jstringserver.server.wait.WaitStrategy;

public class SelectAcceptor implements Runnable {

    private final Selector               serverSelector;
    private final SocketChannelExchanger socketChannelExchanger;
    private final WaitStrategy           waitStrategy;
    private volatile boolean             isRunning = true;

    public SelectAcceptor(Selector serverSelector, 
            SocketChannelExchanger socketChannelExchanger, 
            WaitStrategy waitStrategy) {
        super();
        this.serverSelector = serverSelector;
        this.socketChannelExchanger = socketChannelExchanger;
        this.waitStrategy = waitStrategy;
    }

    @Override
    public void run() {
        while (isRunning) {
            accept();
        }
    }

    private void accept() {
        try {
            while (isRunning) {
                // see http://people.freebsd.org/~jlemon/papers/kqueue.pdf
                // "Kevent - structue which delivers event to user. poll/select does not scale well"
                // - http://people.freebsd.org/~jlemon/kqueue_slides/sld006.htm
                int selected = serverSelector.select(); // sun.nio.ch.KQueueSelectorImpl
                if (selected > 0) {
                    Set<SelectionKey> keys = serverSelector.keys();
                    for (SelectionKey key : keys) {
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        while (socketChannel != null) {
                            socketChannelExchanger.ready(socketChannel);
                            socketChannel = serverSocketChannel.accept();
                        }
                    }
                } else {
                    waitStrategy.pause();
                }
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
