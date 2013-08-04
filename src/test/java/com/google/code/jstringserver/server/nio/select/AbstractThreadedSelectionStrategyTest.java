package com.google.code.jstringserver.server.nio.select;

import static java.nio.channels.SelectionKey.OP_CONNECT;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.code.jstringserver.client.Connector;
import com.google.code.jstringserver.server.ServerBuilder;

public abstract class AbstractThreadedSelectionStrategyTest {
    private     ServerBuilder   serverBuilder;
    protected   Selector        serverSelector;

    private SelectionStrategy toTest;

    @Before
    public void startServer() throws IOException {
        serverBuilder = new ServerBuilder(10);
        serverSelector = Selector.open();
        toTest = strategyToTest();
    }

    protected abstract SelectionStrategy strategyToTest();

    @After
    public void shutdownServer() throws IOException {
        serverBuilder.shutdown();
    }

    @Test
    public void test() throws IOException, InterruptedException {
        SteppingConnector client = connectClient();
        getServerSideChannelForClient();
        invokeSelectInOtherThread();
        postTestChecks();
    }

    protected abstract void postTestChecks();

    private SelectTask invokeSelectInOtherThread() throws InterruptedException {
        SelectTask  target = new SelectTask(1);
        Thread      thread = inSeparateThread(target);
        try {
            boolean timedout = target.afterSelect.await(5, SECONDS);
            assertTrue(timedout);
        } finally {
            thread.interrupt();
        }
        assertFalse(target.failed);
        return target;
    }

    private SocketChannel getServerSideChannelForClient() throws IOException, ClosedChannelException {
        SocketChannel serverSideSocketChannel = serverBuilder.accept();
        serverSideSocketChannel.configureBlocking(false);;
        serverSideSocketChannel.register(serverSelector, OP_READ | OP_WRITE | OP_CONNECT);
        return serverSideSocketChannel;
    }

    private Thread inSeparateThread(SelectTask target) {
        Thread thread = new Thread(target);
        thread.start();
        return thread;
    }
    
    class SelectTask implements Runnable {
        private final int numSelects;
        private volatile boolean failed;
        private final CountDownLatch afterSelect;
        
        public SelectTask(
            int numSelects) {
            super();
            this.numSelects = numSelects;
            this.afterSelect = new CountDownLatch(numSelects);
        }

        @Override
        public void run() {
            try {
                for (int i = 0 ; i < numSelects ; i++) {
                    toTest.select();
                    afterSelect.countDown();
                }
            } catch (IOException e) {
                e.printStackTrace();
                failed = true;
            }
        }
    }

    private SteppingConnector connectClient() {
        SteppingConnector connector = new SteppingConnector(serverBuilder.getAddress(), serverBuilder.getPort());
        Thread thread = new Thread(connector);
        thread.start();
        return connector;
    }

    class SteppingConnector extends Connector {

        public SteppingConnector(
            String address,
            int port) {
            super(address, port, null);
        }

        @Override
        protected void connected(SocketChannel socketChannel) throws IOException {
            send(socketChannel, "hello");
            send(socketChannel, "world");
            socketChannel.close();
        }

        private void send(SocketChannel socketChannel, String toSend) throws IOException {
            ByteBuffer byteBuffer = ByteBuffer.wrap(toSend.getBytes());
            socketChannel.write(byteBuffer);
            flush(socketChannel);
        }

        private void flush(SocketChannel socketChannel) throws IOException {
            socketChannel.socket().getOutputStream().flush();
        }

    }

}
