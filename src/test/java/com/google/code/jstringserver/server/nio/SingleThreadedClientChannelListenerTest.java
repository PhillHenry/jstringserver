package com.google.code.jstringserver.server.nio;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.code.jstringserver.server.FreePortFinder;
import com.google.code.jstringserver.server.bytebuffers.store.ByteBufferStore;
import com.google.code.jstringserver.server.exchange.BlockingSocketChannelExchanger;
import com.google.code.jstringserver.server.exchange.SocketChannelExchanger.ReadyCallback;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;
import com.google.code.jstringserver.server.nio.select.NioReader;
import com.google.code.jstringserver.server.nio.select.NioWriter;
import com.google.code.jstringserver.server.nio.select.SelectionStrategy;
import com.google.code.jstringserver.server.nio.select.SingleThreadedReadingSelectionStrategy;

public class SingleThreadedClientChannelListenerTest {

    private final ClientDataHandler              mockClientDataHandler  = mock(ClientDataHandler.class);
    private final ByteBufferStore                mockByteBufferStore    = mock(ByteBufferStore.class);
    private final BlockingSocketChannelExchanger socketChannelExchanger = new BlockingSocketChannelExchanger();

    private ReadWriteDispatcher  toTest;
    private Thread                               thread;
    private ByteBuffer                           byteBuffer;
    private Thread                               simpleServerThread;
    private SimpleServer                         simpleServer;
    private SimpleClient simpleClient;
    private Thread simpleClientThread;

    @Before
    public void setUp() throws IOException, InterruptedException {
        byteBuffer = ByteBuffer.allocate(1024);
        when(mockByteBufferStore.getByteBuffer()).thenReturn(byteBuffer);
        final Selector            clientSelector    = Selector.open();
        SelectionStrategy selectionStrategy = new SingleThreadedReadingSelectionStrategy(
                                                                                          null, 
                                                                                          clientSelector, 
                                                                                          new NioWriter(mockClientDataHandler, null), 
                                                                                          new NioReader(mockClientDataHandler, mockByteBufferStore, null));
        toTest = new ReadWriteDispatcher(socketChannelExchanger, selectionStrategy, clientSelector);
        socketChannelExchanger.setReadyCallback(new ReadyCallback() {
            
            @Override
            public void ready() {
                clientSelector.wakeup();
            }
        });
        thread = new Thread(toTest);
        thread.start();

        initNetwork();
    }

    @After
    public void tearDown() {
        toTest.shutdown();
        thread.interrupt();
        simpleServerThread.interrupt();
        simpleClientThread.interrupt();
    }

    @Test
    public void shouldProcessConnection() throws IOException, InterruptedException {
        Thread.sleep(100);

        verify(mockClientDataHandler).handleRead(Mockito.eq(byteBuffer), Mockito.any(SelectionKey.class));
    }

    private void initNetwork() throws IOException, InterruptedException {
        FreePortFinder freePortFinder = new FreePortFinder();
        final int port = freePortFinder.getFreePort();
        startSimpleServer(port);
        Thread.sleep(100);
        startSimpleClient(port);
    }

    private void startSimpleClient(final int port) {
        simpleClient = new SimpleClient(port);
        simpleClientThread = new Thread(simpleClient);
        simpleClientThread.start();
    }

    private void startSimpleServer(final int port) {
        simpleServer = new SimpleServer(port);
        simpleServerThread = new Thread(simpleServer);
        simpleServerThread.start();
    }

    class SimpleServer implements Runnable {
        final int port;

        public SimpleServer(int port) {
            super();
            this.port = port;
        }

        @Override
        public void run() {
            ServerSocketChannel serverSocketChannel;
            try {
                serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.bind(createLocalAddress(port));
                SocketChannel socketChannel = serverSocketChannel.accept();
                //socketChannel.finishConnect();
                socketChannelExchanger.ready(socketChannel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private InetSocketAddress createLocalAddress(int port) {
        return new InetSocketAddress("localhost", port);
    }

    class SimpleClient implements Runnable {

        final int port;

        public SimpleClient(int port) {
            super();
            this.port = port;
        }

        @Override
        public void run() {
            try {
                SocketChannel socketChannel = SocketChannel.open(createLocalAddress(port));
                socketChannel.write(ByteBuffer.wrap("testing".getBytes()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }

    }
}
