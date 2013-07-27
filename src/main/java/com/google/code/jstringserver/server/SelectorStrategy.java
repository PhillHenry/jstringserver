package com.google.code.jstringserver.server;

import java.io.IOException;
import java.nio.channels.Selector;
import java.util.concurrent.ThreadPoolExecutor;

import com.google.code.jstringserver.server.exchange.SocketChannelExchanger;
import com.google.code.jstringserver.server.nio.AcceptorDispatcher;
import com.google.code.jstringserver.server.nio.ClientChannelListener;
import com.google.code.jstringserver.server.nio.select.AbstractSelectionStrategy;
import com.google.code.jstringserver.server.wait.WaitStrategy;

public class SelectorStrategy implements ThreadStrategy {

    private final Server                 server;
    private final ThreadPoolExecutor     threadPoolExecutor;
    private final int                    numThreads;
    private final SocketChannelExchanger socketChannelExchanger;
    private final WaitStrategy           waitStrategy;
    private final ClientChannelListener  clientChannelListener;
    private final AbstractSelectionStrategy acceptorStrategy;

    private AcceptorDispatcher               selectorAcceptor;


    public SelectorStrategy(Server server,
                            int numThreads,
                            SocketChannelExchanger socketChannelExchanger,
                            WaitStrategy waitStrategy,
                            ClientChannelListener  clientChannelListener, 
                            AbstractSelectionStrategy acceptorStrategy) throws IOException {
        this.numThreads = numThreads;
        this.socketChannelExchanger = socketChannelExchanger;
        this.waitStrategy = waitStrategy;
        this.clientChannelListener = clientChannelListener;
        this.acceptorStrategy = acceptorStrategy;
        this.server = server;
        configCallback();
        threadPoolExecutor = new ThreadPoolFactory().createThreadPoolExecutor(1);
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
        for (int i = 0; i < numThreads; i++) {
            threadPoolExecutor.execute(clientChannelListener);
        }
    }

    private void startAcceptorThread() throws IOException {
        selectorAcceptor = new AcceptorDispatcher(server, socketChannelExchanger, waitStrategy, acceptorStrategy);
        Thread acceptorThread = new Thread(selectorAcceptor, "acceptor thread");
        acceptorThread.start();
    }

    @Override
    public void shutdown() {
        clientChannelListener.shutdown();
        threadPoolExecutor.shutdown();
        shutdownAcceptor();
        shutdownSelector();
    }

    private void shutdownSelector() {
        try {
            clientChannelListener.getSelector().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void shutdownAcceptor() {
        if (selectorAcceptor != null) {
            try {
                selectorAcceptor.shutdown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
