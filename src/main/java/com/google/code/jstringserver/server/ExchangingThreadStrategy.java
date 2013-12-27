package com.google.code.jstringserver.server;

import java.io.IOException;

import com.google.code.jstringserver.server.exchange.SocketChannelExchanger;
import com.google.code.jstringserver.server.nio.AcceptorDispatcher;
import com.google.code.jstringserver.server.nio.ClientChannelListener;
import com.google.code.jstringserver.server.nio.select.AbstractSelectionStrategy;
import com.google.code.jstringserver.server.wait.WaitStrategy;

public class ExchangingThreadStrategy implements ThreadStrategy {

    private final SocketChannelExchanger    socketChannelExchanger;
    private final ClientChannelListener[]   clientChannelListener;
    private final AcceptorDispatcher        selectorAcceptor;

    public ExchangingThreadStrategy(
                            Server                      server,
                            SocketChannelExchanger      socketChannelExchanger,
                            WaitStrategy                waitStrategy,
                            AbstractSelectionStrategy   acceptorStrategy, 
                            ClientChannelListener...    clientChannelListener) throws IOException {
        this.socketChannelExchanger = socketChannelExchanger;
        this.clientChannelListener  = clientChannelListener;
        selectorAcceptor            = new AcceptorDispatcher(server, socketChannelExchanger, waitStrategy, acceptorStrategy);
        configCallback();
    }

    private void configCallback() {
        this.socketChannelExchanger.setReadyCallback(new SocketChannelExchanger.ReadyCallback() {
            @Override
            public void ready() {
                for (int i = 0 ; i < clientChannelListener.length ; i++) {
                    clientChannelListener[i].getSelector().wakeup();
                }
            }
        });
    }

    @Override
    public void start() throws IOException {
        startAcceptorThread();
        startListenerThreads();
    }

    private void startListenerThreads() {
        for (int i = 0 ; i < clientChannelListener.length ; i++) {
            Thread clientHandlingThread = new Thread(clientChannelListener[i], "client handler " + i);
            clientHandlingThread.start();
        }
    }

    private void startAcceptorThread() throws IOException {
        Thread acceptorThread = new Thread(selectorAcceptor, "acceptor thread");
        acceptorThread.start();
    }

    @Override
    public void shutdown() {
        for (int i = 0 ; i < clientChannelListener.length ; i++) {
            clientChannelListener[i].shutdown();
        }
        selectorAcceptor.shutdown();
    }

}
