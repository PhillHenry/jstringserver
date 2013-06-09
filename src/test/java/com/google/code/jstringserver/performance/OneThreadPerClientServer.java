package com.google.code.jstringserver.performance;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.code.jstringserver.server.Connector;
import com.google.code.jstringserver.server.FreePortFinder;
import com.google.code.jstringserver.server.OneThreadPerClient;
import com.google.code.jstringserver.server.Server;
import com.google.code.jstringserver.server.ThreadedTaskBuilder;
import com.google.code.jstringserver.server.bytebuffers.factories.ByteBufferFactory;
import com.google.code.jstringserver.server.bytebuffers.factories.DirectByteBufferFactory;
import com.google.code.jstringserver.server.bytebuffers.store.ByteBufferStore;
import com.google.code.jstringserver.server.bytebuffers.store.ThreadLocalByteBufferStore;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;
import com.google.code.jstringserver.server.handlers.ClientReader;

public class OneThreadPerClientServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        OneThreadPerClientServer app = new OneThreadPerClientServer();
        app.setUpServer();
        app.shouldProcessAllCalls();
    }

    private String                   address = "localhost";
    private int                      port;
    private Server                   server;
    private TestingClientDataHandler clientDataHandler;
    private OneThreadPerClient       oneThreadPerClient;
    private int                      numClients;
    
    @Before
    public void setUpServer() throws IOException {
        FreePortFinder  freePortFinder  = new FreePortFinder();
        port                            = freePortFinder.getFreePort();
        server                          = new Server(address, port, true);
        clientDataHandler               = new TestingClientDataHandler();
        oneThreadPerClient              = threadingStrategy(server, clientDataHandler);
        numClients                      = 200;
        server.connect();
    }
    
    @After
    public void tearDownServer() throws InterruptedException, IOException {
        oneThreadPerClient.shutdown();
        server.shutdown();
    }
    
    @Test
    public void shouldProcessAllCalls() throws IOException, InterruptedException {
        runConnectors(port, address, oneThreadPerClient);
        waitForNumberOfExpectedCalls(20, 2000, clientDataHandler);

        assertEquals("Total number of calls completed on server side", 
                numClients, clientDataHandler.numEndCalls.get());
    }
    
    private boolean waitForNumberOfExpectedCalls(int expectedNumCalls, long timeoutMs, TestingClientDataHandler clientDataHandler) throws InterruptedException {
        long start = System.currentTimeMillis();
        while (clientDataHandler.numEndCalls.get() != expectedNumCalls) {
            Thread.sleep(100);
            if (System.currentTimeMillis() > (start + timeoutMs)) return false;
        }
        return true;
    }

    private void runConnectors(int port, String address, OneThreadPerClient oneThreadPerClient) throws InterruptedException {
        oneThreadPerClient.start();
        ThreadedTaskBuilder threadedTaskBuilder = new ThreadedTaskBuilder();
        Connector[]         connectors          = Connector.createConnectors(numClients, address, port);
        System.out.println("Starting threads");
        long                start               = System.currentTimeMillis();
        Thread[]            threads             = threadedTaskBuilder.start(connectors, "connector");
        threadedTaskBuilder.join(threads, 10000);
        System.out.println("Test took " + (System.currentTimeMillis() - start) + "ms");
        threadedTaskBuilder.interruptAllThreads();
    }

    private OneThreadPerClient threadingStrategy(Server server, ClientDataHandler clientDataHandler) {
        ByteBufferFactory   byteBufferFactory   = new DirectByteBufferFactory(4096);
        ByteBufferStore     byteBufferStore     = new ThreadLocalByteBufferStore(byteBufferFactory );
        
        ClientReader        clientHandler       = new ClientReader(byteBufferStore , clientDataHandler );
        OneThreadPerClient  oneThreadPerClient  = new OneThreadPerClient(server, 8, clientHandler);
        return oneThreadPerClient;
    }
    
    static class TestingClientDataHandler implements ClientDataHandler {
        
        private final AtomicInteger numEndCalls = new AtomicInteger();
        
        private final AtomicLong numBytesData = new AtomicLong();

        @Override
        public void handle(ByteBuffer byteBuffer) {
            numBytesData.addAndGet(byteBuffer.position());
        }

        @Override
        public void end() {
            numEndCalls.incrementAndGet();
        }
        
    }

}
