package com.google.code.jstringserver.server.nio;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import com.google.code.jstringserver.server.exchange.SocketChannelExchanger;
import com.google.code.jstringserver.server.nio.select.SelectionStrategy;

public class ReadWriteDispatcher implements ClientChannelListener {

    private final SocketChannelExchanger    socketChannelExchanger;
    private final SelectionStrategy         selectionStrategy;
    private final Selector                  selector;
    private final ClientConfigurer          clientConfigurer;
    
    private volatile boolean                isRunning = true;

    public ReadWriteDispatcher(SocketChannelExchanger socketChannelExchanger, 
                               SelectionStrategy selectionStrategy, 
                               Selector selector) {
        super();
        this.socketChannelExchanger = socketChannelExchanger;
        this.selectionStrategy      = selectionStrategy;
        this.selector               = selector;
        this.clientConfigurer       = new ClientConfigurer(new SimpleSelectorHolder(selector));
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                SocketChannel socketChannel = socketChannelExchanger.consume();
                if (socketChannel != null) {
                    clientConfigurer.register(socketChannel);
                }
                checkIncoming();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
