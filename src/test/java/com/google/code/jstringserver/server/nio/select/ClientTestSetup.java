package com.google.code.jstringserver.server.nio.select;

import static com.google.code.jstringserver.server.nio.select.ServerTestSetup.HOST;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.google.code.jstringserver.client.LatchedWritingConnector;
import com.google.code.jstringserver.server.bytebuffers.factories.DirectByteBufferFactory;
import com.google.code.jstringserver.server.bytebuffers.store.ThreadLocalByteBufferStore;

public class ClientTestSetup {
    
    private final CountDownLatch preWrite;
    private final CountDownLatch postWrite;
    private final CountDownLatch preRead;
    private final CountDownLatch postRead;
    private final CountDownLatch postClose;
    
    private final ServerTestSetup serverTestSetup;
    private final int numClients;
    
    public ClientTestSetup(
        ServerTestSetup serverTestSetup,
        int numClients) {
        super();
        this.serverTestSetup = serverTestSetup;
        this.numClients = numClients;
        preWrite = new CountDownLatch(numClients);
        preRead = new CountDownLatch(numClients);
        postWrite = new CountDownLatch(numClients);
        postRead = new CountDownLatch(numClients);
        postClose = new CountDownLatch(numClients);
    }

    public LatchedWritingConnector[] createLatchedClients() {

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
        return writers;
    }
    
    public void awaitPostClose() throws InterruptedException {
        await(postClose);
    }
    
    public static void await(CountDownLatch latch) throws InterruptedException {
        await(latch, 1000);
    }
    
    public static void await(CountDownLatch latch, long timeoutMs) throws InterruptedException {
        boolean noTimeout = latch.await(timeoutMs, TimeUnit.MILLISECONDS);
        assertTrue(noTimeout);
    }

    public void awaitPostRead() throws InterruptedException {
        await(postRead);
    }

    public void awaitPreWrite() throws InterruptedException {
        await(preWrite);
    }
}
