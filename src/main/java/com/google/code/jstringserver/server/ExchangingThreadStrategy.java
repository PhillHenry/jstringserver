package com.google.code.jstringserver.server;

import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

import com.google.code.jstringserver.server.exchange.SocketChannelExchanger;
import com.google.code.jstringserver.server.nio.AcceptorDispatcher;
import com.google.code.jstringserver.server.nio.ClientChannelListener;
import com.google.code.jstringserver.server.nio.select.AbstractSelectionStrategy;
import com.google.code.jstringserver.server.wait.WaitStrategy;

public class ExchangingThreadStrategy implements ThreadStrategy {

    private final SocketChannelExchanger socketChannelExchanger;
    private final ClientChannelListener  clientChannelListener;
    private final AcceptorDispatcher     selectorAcceptor;

    public ExchangingThreadStrategy(
                            Server                      server,
                            SocketChannelExchanger      socketChannelExchanger,
                            WaitStrategy                waitStrategy,
                            ClientChannelListener       clientChannelListener, 
                            AbstractSelectionStrategy   acceptorStrategy) throws IOException {
        this.socketChannelExchanger = socketChannelExchanger;
        this.clientChannelListener  = clientChannelListener;
        selectorAcceptor            = new AcceptorDispatcher(server, socketChannelExchanger, waitStrategy, acceptorStrategy);
        configCallback();
    }

    private void configCallback() {
        this.socketChannelExchanger.setReadyCallback(new SocketChannelExchanger.ReadyCallback() {
            @Override
            public void ready() {
                clientChannelListener.getSelector().wakeup();
            }
        });
    }

    @Override
    public void start() throws IOException {
        startAcceptorThread();
        startListenerThreads();
    }

    private void startListenerThreads() {
        Thread clientHandlingThread = new Thread(clientChannelListener);
        clientHandlingThread.start();
    }

    private void startAcceptorThread() throws IOException {
        Thread acceptorThread = new Thread(selectorAcceptor, "acceptor thread");
        acceptorThread.start();
    }

    @Override
    public void shutdown() {
        clientChannelListener.shutdown();
        selectorAcceptor.shutdown();
    }

}
