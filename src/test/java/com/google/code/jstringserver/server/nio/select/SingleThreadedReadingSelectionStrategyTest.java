package com.google.code.jstringserver.server.nio.select;

import static com.google.code.jstringserver.client.Connector.createConnectors;
import static com.google.code.jstringserver.server.nio.select.ServerTestSetup.HOST;
import static java.net.StandardSocketOptions.SO_LINGER;
import static org.junit.Assert.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

import com.google.code.jstringserver.client.Connector;
import com.google.code.jstringserver.client.LatchedWritingConnector;
import com.google.code.jstringserver.client.WritingConnector;
import com.google.code.jstringserver.server.AbstractMultiThreadedTest;
import com.google.code.jstringserver.server.bytebuffers.factories.DirectByteBufferFactory;
import com.google.code.jstringserver.server.bytebuffers.store.ThreadLocalByteBufferStore;
import com.google.code.jstringserver.server.nio.ClientConfigurer;

public class SingleThreadedReadingSelectionStrategyTest extends AbstractMultiThreadedTest {
    
    private SingleThreadedReadingSelectionStrategy toTest;
    
    private ServerTestSetup serverTestSetup;
    
    private AbstractNioWriter writer = mock(AbstractNioWriter.class);
    private AbstractNioReader reader = mock(AbstractNioReader.class);
    
    private CountDownLatch preWrite;
    private CountDownLatch postWrite;
    private CountDownLatch preRead;
    private CountDownLatch postRead;
    private CountDownLatch postClose;

    private Selector clientSelector;

    @Before
    public void setUp() throws IOException {
        serverTestSetup = new ServerTestSetup();
        clientSelector = Selector.open();
        toTest = new SingleThreadedReadingSelectionStrategy(null, clientSelector, writer, reader);
    }
    
    @After
    public void shutdown() throws IOException {
        serverTestSetup.shutdown();
    }
    
    @Test 
    @Ignore // don't know how we can read/write when the client has disconnected...
    public void disconnectsImmediately() throws IOException, InterruptedException {
        clientsConnectThenDisconnect();
        await(postClose);
        toTest.select();
        checkReadAndWrite(0);
    }
    
    @Test
    public void selectLifecycle() throws IOException, InterruptedException {
        clientsConnectAndReadyForReadWrite();
        
        await(postRead);
        await(preWrite);
        toTest.select();
        checkReadAndWrite(1);
        
    }
    
    private void await(CountDownLatch latch) throws InterruptedException {
        await(latch, 1000);
    }
    
    private void await(CountDownLatch latch, long timeoutMs) throws InterruptedException {
        boolean noTimeout = latch.await(timeoutMs, TimeUnit.MILLISECONDS);
        assertTrue(noTimeout);
    }

    private void checkReadAndWrite(int expected) throws IOException {
        VerificationMode times = expected == 0 ? Mockito.never() : times(expected);
        verify(writer, times).write(any(SelectionKey.class), any(SocketChannel.class));
        verify(reader, times).read(any(SelectionKey.class), any(SocketChannel.class));
    }

    private void clientsConnectThenDisconnect() throws IOException {
        Connector[] connectors = new Connector[] { 
            new Connector(HOST, serverTestSetup.getPort()) {
                
                

                @Override
                protected void configure(InetSocketAddress inetSocketAddress, SocketChannel socketChannel) throws IOException {
                    super.configure(inetSocketAddress, socketChannel);
                    socketChannel.setOption(SO_LINGER, 0);
                }

                @Override
                protected void close(SocketChannel socketChannel) throws IOException {
                    socketChannel.configureBlocking(true);
                    super.close(socketChannel);
                    socketChannel.socket().close();
                    postClose.countDown();
                }
                
            }
        };
        postClose = new CountDownLatch(connectors.length);
        start(connectors, "connectors");
        serverTestSetup.accept(clientSelector);
    }
    
    private void clientsConnectAndReadyForReadWrite() throws IOException {
        int numClients = 1;
        preWrite = new CountDownLatch(numClients);
        preRead = new CountDownLatch(numClients);
        postWrite = new CountDownLatch(numClients);
        postRead = new CountDownLatch(numClients);
        postClose = new CountDownLatch(numClients);
        LatchedWritingConnector[] writers = LatchedWritingConnector.createWritingConnectors(
            numClients, 
            HOST, 
            serverTestSetup.getPort(), 
            "payload", 
            new ThreadLocalByteBufferStore(new DirectByteBufferFactory(1024)),
            preWrite,
            postWrite,
            preRead, 
            postRead, 
            postClose);
        start(writers, "rw");
        serverTestSetup.accept(clientSelector);
    }

}
