package com.google.code.jstringserver.server.nio;

import static java.nio.channels.SelectionKey.OP_CONNECT;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import com.google.code.jstringserver.server.exchange.SocketChannelExchanger;
import com.google.code.jstringserver.server.nio.select.AbstractSelectionStrategy;

public class ReadWriteDispatcher implements ClientChannelListener {

    private final SocketChannelExchanger    socketChannelExchanger;
    private final AbstractSelectionStrategy selectionStrategy;
    private final Selector                  selector;
    
    private volatile boolean                isRunning = true;

    public ReadWriteDispatcher(SocketChannelExchanger socketChannelExchanger, 
                               AbstractSelectionStrategy selectionStrategy, 
                               Selector selector) {
        super();
        this.socketChannelExchanger = socketChannelExchanger;
        this.selectionStrategy = selectionStrategy;
        this.selector = selector;
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
        try {
            selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Selector getSelector() {
        return selector;
    }

}
