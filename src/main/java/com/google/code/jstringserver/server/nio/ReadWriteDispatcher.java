package com.google.code.jstringserver.server.nio;

import static java.nio.channels.SelectionKey.OP_CONNECT;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import com.google.code.jstringserver.server.bytebuffers.store.ByteBufferStore;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;

public class ReadWriteDispatcher implements ClientChannelListener {

    private final SocketChannelExchanger    socketChannelExchanger;
    private final AbstractSelectionStrategy selectionStrategy;

    private volatile Selector               selector;
    private volatile boolean                isRunning = true;

    public ReadWriteDispatcher(SocketChannelExchanger socketChannelExchanger, 
                               AbstractSelectionStrategy selectionStrategy) {
        super();
        this.socketChannelExchanger = socketChannelExchanger;
        this.selectionStrategy = selectionStrategy;
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                SocketChannel socketChannel = socketChannelExchanger.consume();
                if (socketChannel != null) {
                    register(socketChannel);
                }
                checkIncoming();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void register(SocketChannel socketChannel) throws IOException, ClosedChannelException {
        if (socketChannel != null) {
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, OP_READ | OP_CONNECT | OP_WRITE); 
        }
    }

    private void checkIncoming() throws IOException {
        selectionStrategy.select();
    }

    public void shutdown() {
        isRunning = false;
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
        selectionStrategy.setSelector(selector);
    }

}
