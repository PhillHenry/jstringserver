package com.google.code.jstringserver.performance;

import static com.google.code.jstringserver.client.WritingConnector.createWritingConnectors;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.channels.Selector;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.code.jstringserver.client.WritingConnector;
import com.google.code.jstringserver.server.Server;
import com.google.code.jstringserver.server.ServerBuilder;
import com.google.code.jstringserver.server.ThreadStrategy;
import com.google.code.jstringserver.server.ThreadedTaskBuilder;
import com.google.code.jstringserver.server.bytebuffers.factories.ByteBufferFactory;
import com.google.code.jstringserver.server.bytebuffers.factories.DirectByteBufferFactory;
import com.google.code.jstringserver.server.bytebuffers.store.ByteBufferStore;
import com.google.code.jstringserver.server.bytebuffers.store.ThreadLocalByteBufferStore;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;
import com.google.code.jstringserver.server.handlers.ClientReader;
import com.google.code.jstringserver.server.handlers.NaiveClientReader;

public abstract class AbstractThreadStrategyTest<T extends ThreadStrategy> {

    public static final int          payloadSize = 10007;
    String                           payload;

    protected ClientDataHandler      clientDataHandler;
    protected Selector               clientSelector;
    protected Selector               serverSelector;
    private T                        threadStrategy;
    private int                      numClients;
    private ThreadedTaskBuilder      threadedTaskBuilder;
    private ServerBuilder            serverBuilder;
    
    @Before
    public void setUpServer() throws Exception {
        clientSelector                  = Selector.open();
        serverSelector                  = Selector.open();
        numClients                      = getNumberOfClients();
        serverBuilder                   = new ServerBuilder(numClients);
        payload                         = getPayload();
        clientDataHandler               = createClientDataHandler();
        threadStrategy                  = threadingStrategy(serverBuilder.getServer(), clientDataHandler);
        threadedTaskBuilder             = new ThreadedTaskBuilder();
        threadStrategy.start();
    }

    protected int getNumberOfClients() {
        return 100;
    }

    protected static int availableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }
    
    protected ClientDataHandler createClientDataHandler() {
        return new ThreadLocalClientDataHandler(payload);
    }
    
    protected abstract T threadingStrategy(Server server, ClientDataHandler clientDataHandler) throws IOException;
    
    protected abstract void checkThreadStrategy(T threadStrategy);
    
    protected T getThreadStrategy() {
        return threadStrategy;
    }

    @After
    public void tearDownServer() throws InterruptedException, IOException {
        System.out.println("Shutting down server");
        threadStrategy.shutdown();
        threadedTaskBuilder.interruptAllThreads();
        serverBuilder.getServer().shutdown();
    }
    
    @Test
    public void shouldProcessAllCalls() throws IOException, InterruptedException {
        runConnectors(serverBuilder.getPort(), serverBuilder.getAddress(), threadStrategy);
        waitForNumberOfExpectedCalls(numExpectedCalls(), 5000, clientDataHandler);
        checkThreadStrategy(threadStrategy);
        assertEquals("Total number of calls completed on server side", 
                numExpectedCalls(), clientDataHandler.getNumEndCalls());
    }
    
    protected int numExpectedCalls() {
        return numClients;
    }
    
    private boolean waitForNumberOfExpectedCalls(int expectedNumCalls, long timeoutMs, ClientDataHandler clientDataHandler) throws InterruptedException {
        long start = System.currentTimeMillis();
        try {
            while (clientDataHandler.getNumEndCalls() != expectedNumCalls) {
                Thread.sleep(10);
                if (System.currentTimeMillis() > (start + timeoutMs)) return false;
            }
        } finally {
            System.out.println("Waiting for all threads to finish took " + (System.currentTimeMillis() - start) + "ms");
            System.out.println(String.format("Expected number of calls %d. Actual number %d", expectedNumCalls, clientDataHandler.getNumEndCalls()));
        }
        return true;
    }

    private void runConnectors(int port, String address, ThreadStrategy threadStrategy2) throws InterruptedException {
        WritingConnector[]  connectors          = createWritingConnectors(numClients, address, port, payload, getByteBufferStore());
        System.out.println("Starting threads");
        long                start               = System.currentTimeMillis();
        Thread[]            threads             = threadedTaskBuilder.start(connectors, "connector");
        threadedTaskBuilder.join(threads, 10000);
    }

    public static String getPayload() {
        StringBuffer stringBuffer = new StringBuffer();
        int padSize = 10;
        int i = 0;
        for (i = 0 ; i < payloadSize ; i+=padSize) {
            String padded = String.format("%" + padSize +"d", i);
            stringBuffer.append(padded);
        }
        for (int j = i ; j < payloadSize ; j++) {
            stringBuffer.append("x");
        }
        return stringBuffer.toString();
    }


    protected ByteBufferStore getByteBufferStore() {
        ByteBufferFactory   byteBufferFactory   = new DirectByteBufferFactory(4096);
        ByteBufferStore     byteBufferStore     = new ThreadLocalByteBufferStore(byteBufferFactory);
        return byteBufferStore;
    }

    protected ClientReader createClientReader(ClientDataHandler clientDataHandler) {
        ByteBufferStore     byteBufferStore     = getByteBufferStore();
        ClientReader        clientHandler       = new NaiveClientReader(byteBufferStore , clientDataHandler );
        return clientHandler;
    }


}
