package com.google.code.jstringserver.server;

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

import org.junit.Test;

import com.google.code.jstringserver.client.Connector;

public class Nio2ServerTest extends AbstractNio2ServerTest {
    
    private final AtomicBoolean     connected   = new AtomicBoolean(false);
    private final AtomicBoolean     failed      = new AtomicBoolean(false);
    
    @Test
    public void shouldListen() throws IOException, InterruptedException {
        final CountDownLatch    latch   = new CountDownLatch(1);
        registerSimpleCompletionHandler(latch);
        
        connect1Thread(latch);
        
        assertFalse(failed.get());
        assertTrue(connected.get());
    }

    private void connect1Thread(final CountDownLatch latch) throws InterruptedException {
        Connector[] connectors = startConnectors();
        latch.await(100, MILLISECONDS);
        checkFinished(connectors);
        checkNotInError(connectors);
    }

    private void registerSimpleCompletionHandler(final CountDownLatch latch) throws IOException {
        nio2ServerBuilder.getServer().register(new CompletionHandler<AsynchronousSocketChannel, Object>() {
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
