package com.google.code.jstringserver.server;

import static java.nio.channels.SelectionKey.OP_CONNECT;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import com.google.code.jstringserver.server.bytebuffers.store.ByteBufferStore;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;
import com.google.code.jstringserver.server.nio.ClientChannelListener;
import com.google.code.jstringserver.server.nio.SingleThreadedClientChannelListener;
import com.google.code.jstringserver.server.nio.SelectAcceptor;
import com.google.code.jstringserver.server.nio.SocketChannelExchanger;
import com.google.code.jstringserver.server.wait.WaitStrategy;

public class SelectorStrategy implements ThreadStrategy {

    private final Selector               selector;

    private final Server                 server;
    private final ThreadPoolExecutor     threadPoolExecutor;
    private final int                    numThreads;
    private final SocketChannelExchanger socketChannelExchanger;
    private final WaitStrategy           waitStrategy;
    private final ClientChannelListener  clientChannelListener;

    private SelectAcceptor               selectorAcceptor;

    public SelectorStrategy(Server server,
                            int numThreads,
                            SocketChannelExchanger socketChannelExchanger,
                            WaitStrategy waitStrategy,
                            ClientChannelListener  clientChannelListener) throws IOException {
        this.numThreads = numThreads;
        this.socketChannelExchanger = socketChannelExchanger;
        this.waitStrategy = waitStrategy;
        this.clientChannelListener = clientChannelListener;
        this.selector = Selector.open();
        this.server = server;
        clientChannelListener.setSelector(selector);
        configCallback();
        threadPoolExecutor = new ThreadPoolFactory().createThreadPoolExecutor(1);
    }

    private void configCallback() {
        this.socketChannelExchanger.setReadyCallback(new SocketChannelExchanger.ReadyCallback() {
            @Override
            public void ready() {
                selector.wakeup();
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
        Selector serverSelector = Selector.open();
        selectorAcceptor = new SelectAcceptor(server, serverSelector, socketChannelExchanger, waitStrategy);
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
            selector.close();
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
