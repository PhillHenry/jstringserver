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
import com.google.code.jstringserver.server.nio.SelectAcceptor;
import com.google.code.jstringserver.server.nio.SocketChannelExchanger;
import com.google.code.jstringserver.server.wait.WaitStrategy;

public class SelectorStrategy implements ThreadStrategy {

    private final Selector               selector;

    private final Server                 server;
    private final ThreadPoolExecutor     threadPoolExecutor;
    private final int                    numThreads;
    private final ClientDataHandler      clientDataHandler;
    private final ByteBufferStore        byteBufferStore;
    private final SocketChannelExchanger socketChannelExchanger;
    private final WaitStrategy           waitStrategy;

    private volatile boolean             isRunning = true;

    private SelectAcceptor               selectorAcceptor;

    public SelectorStrategy(Server server,
                            int numThreads,
                            ClientDataHandler clientDataHandler,
                            ByteBufferStore byteBufferStore,
                            SocketChannelExchanger socketChannelExchanger,
                            WaitStrategy waitStrategy) throws IOException {
        this.numThreads = numThreads;
        this.clientDataHandler = clientDataHandler;
        this.socketChannelExchanger = socketChannelExchanger;
        this.waitStrategy = waitStrategy;
        this.selector = Selector.open();
        this.socketChannelExchanger.setReadyCallback(new SocketChannelExchanger.ReadyCallback() {

            @Override
            public void ready() {
                selector.wakeup();
            }
        });
        this.server = server;
        this.byteBufferStore = byteBufferStore;
        threadPoolExecutor = new ThreadPoolFactory().createThreadPoolExecutor(1);
    }

    @Override
    public void start() throws IOException {
        startAcceptorThread();
        startListenerThreads();
    }

    private void startListenerThreads() {
        for (int i = 0; i < numThreads; i++) {
            threadPoolExecutor.execute(createListenerRunnable());
        }
    }

    private Runnable createListenerRunnable() {
        return new Runnable() {

            @Override
            public void run() {
                while (isRunning) {
                    try {
                        SocketChannel socketChannel = socketChannelExchanger.consume();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, OP_READ | OP_WRITE | OP_CONNECT);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    checkIncoming();
                }
            }

        };
    }

    private void checkIncoming() {
        try {
            int selected = selector.select();// "it can return 0 if the wakeup( ) method of the selector is invoked by another thread."
            if (selected > 0) {
                Set<SelectionKey> keys = selector.keys();
                for (SelectionKey key : keys) {
                    SocketChannel selectableChannel = (SocketChannel) key.channel();
                    ByteBuffer byteBuffer = byteBufferStore.getByteBuffer();
                    selectableChannel.read(byteBuffer);
                    byteBuffer.flip();
                    clientDataHandler.handle(byteBuffer);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startAcceptorThread() throws IOException {
        Selector serverSelector = Selector.open();
        server.register(serverSelector);
        selectorAcceptor = new SelectAcceptor(serverSelector, socketChannelExchanger, waitStrategy);
        Thread acceptorThread = new Thread(selectorAcceptor, "acceptor thread");
        acceptorThread.start();
    }

    @Override
    public void shutdown() {
        isRunning = false;
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
