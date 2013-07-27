package com.google.code.jstringserver.server.nio;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

import com.google.code.jstringserver.server.Server;
import com.google.code.jstringserver.server.exchange.SocketChannelExchanger;
import com.google.code.jstringserver.server.nio.select.AbstractSelectionStrategy;
import com.google.code.jstringserver.server.wait.WaitStrategy;

public class AcceptorDispatcher implements Runnable {

    private final AbstractSelectionStrategy selectionStrategy;
    private final Server                    server;

    private volatile boolean                isRunning = true;

    public AcceptorDispatcher(
        Server                      server,
        SocketChannelExchanger      socketChannelExchanger,
        WaitStrategy                waitStrategy,
        AbstractSelectionStrategy   selectionStrategy) {
        super();
        this.server             = server;
        this.selectionStrategy  = selectionStrategy;
    }

    @Override
    public void run() {
        try {
            server.register(selectionStrategy.getSelector());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        doAccept();
    }

    private void doAccept() {
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
        selectionStrategy.getSelector().close();
    }
}
