package com.google.code.jstringserver.server;

import static com.google.code.jstringserver.client.Connector.createConnectors;
import static com.google.code.jstringserver.client.Networker.checkFinished;
import static com.google.code.jstringserver.client.Networker.checkNotInError;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.code.jstringserver.client.Connector;

public class Nio2ServerTest extends AbstractMultiThreadedTest {
    
    private static final String address = "127.0.0.1";
    
    final AtomicBoolean     connected   = new AtomicBoolean(false);
    final AtomicBoolean     failed      = new AtomicBoolean(false);
    
    private Nio2Server      server;
    private int             port;

    @Before
    public void setUp() throws Exception {
        port    = new FreePortFinder().getFreePort();
        server  = new Nio2Server(address, port, 100);
        server.connect();
    }
    
    @After
    public void tearDown() throws Exception {
        if (server != null) {
            server.close();
        }
    }

    @Test
    public void shouldListen() throws IOException, InterruptedException {
        final CountDownLatch    latch   = new CountDownLatch(1);
        registerSimpleCompletionHandler(latch);
        
        connect1Thread(latch);
        
        assertFalse(failed.get());
        assertTrue(connected.get());
    }

    private void connect1Thread(final CountDownLatch latch) throws InterruptedException {
        Connector[] connectors          = createConnectors(1, address, port);
        Thread[]    acceptorThreads     = start(connectors, "connector");
        join(acceptorThreads, 1000L);
        latch.await(100, MILLISECONDS);
        checkFinished(connectors);
        checkNotInError(connectors);
    }

    private void registerSimpleCompletionHandler(final CountDownLatch latch) throws IOException {
        server.register(new CompletionHandler<AsynchronousSocketChannel, Object>() {
            @Override
            public void completed(AsynchronousSocketChannel result, Object attachment) {
                connected.set(true);
                latch.countDown();
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                failed.set(true);
            }
        });
    }

}
