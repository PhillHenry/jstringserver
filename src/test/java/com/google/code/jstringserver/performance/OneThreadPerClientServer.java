package com.google.code.jstringserver.performance;

import static com.google.code.jstringserver.server.WritingConnector.createWritingConnectors;
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
import com.google.code.jstringserver.server.WritingConnector;
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
    
    private final int                payloadSize = 10007;
    private String                   payload;
    private String                   address = "localhost";
    private int                      port;
    private Server                   server;
    private TestingClientDataHandler clientDataHandler;
    private OneThreadPerClient       oneThreadPerClient;
    private int                      numClients;
    private ThreadedTaskBuilder      threadedTaskBuilder;
    
    @Before
    public void setUpServer() throws IOException {
        FreePortFinder  freePortFinder  = new FreePortFinder();
        port                            = freePortFinder.getFreePort();
        server                          = new Server(address, port, true);
        clientDataHandler               = new TestingClientDataHandler();
        oneThreadPerClient              = threadingStrategy(server, clientDataHandler);
        numClients                      = 100;
        threadedTaskBuilder             = new ThreadedTaskBuilder();
        payload                         = getPayload();
        server.connect();
        oneThreadPerClient.start();
    }
    
    @After
    public void tearDownServer() throws InterruptedException, IOException {
        oneThreadPerClient.shutdown();
        threadedTaskBuilder.interruptAllThreads();
        server.shutdown();
    }
    
    @Test
    public void shouldProcessAllCalls() throws IOException, InterruptedException {
        runConnectors(port, address, oneThreadPerClient);
        waitForNumberOfExpectedCalls(20, 1000, clientDataHandler);

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
        WritingConnector[]  connectors          = createWritingConnectors(numClients, address, port, payload, getByteBufferStore());
        System.out.println("Starting threads");
        long                start               = System.currentTimeMillis();
        Thread[]            threads             = threadedTaskBuilder.start(connectors, "connector");
        threadedTaskBuilder.join(threads, 10000);
        System.out.println("Test took " + (System.currentTimeMillis() - start) + "ms");
        
    }

    private String getPayload() {
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

    private OneThreadPerClient threadingStrategy(Server server, ClientDataHandler clientDataHandler) {
        ByteBufferStore     byteBufferStore     = getByteBufferStore();
        ClientReader        clientHandler       = new ClientReader(byteBufferStore , clientDataHandler );
        OneThreadPerClient  oneThreadPerClient  = new OneThreadPerClient(server, 8, clientHandler);
        return oneThreadPerClient;
    }

    private ByteBufferStore getByteBufferStore() {
        ByteBufferFactory   byteBufferFactory   = new DirectByteBufferFactory(4096);
        ByteBufferStore     byteBufferStore     = new ThreadLocalByteBufferStore(byteBufferFactory);
        return byteBufferStore;
    }
    
    class TestingClientDataHandler implements ClientDataHandler {
        
        private final AtomicInteger numEndCalls = new AtomicInteger();
        
        private final AtomicLong numBytesData = new AtomicLong();
        
        private final ThreadLocal<Integer> currentBatchSize = new ThreadLocal<Integer>() {

            @Override
            protected Integer initialValue() {
                return 0;
            }
            
        };
        
        private final ThreadLocal<StringBuffer> receivedPayload = new ThreadLocal<StringBuffer>() {

            @Override
            protected StringBuffer initialValue() {
                return new StringBuffer();
            }
            
        };

        @Override
        public void handle(ByteBuffer byteBuffer) {
            byte[] bytes = new byte[byteBuffer.limit()];
            byteBuffer.get(bytes);
            int filled = byteBuffer.limit(); //A buffer's limit is the index of the first element that should *not* be read or written - http://docs.oracle.com/javase/6/docs/api/java/nio/Buffer.html
            numBytesData.addAndGet(filled);
            currentBatchSize.set(currentBatchSize.get() +  filled);
            receivedPayload.get().append(new String(bytes));
        }

        @Override
        public String end() {
            numEndCalls.incrementAndGet();
            currentBatchSize.set(0);
            checkReceivedPayloadAndRest();
            
            return "OK";
        }

        private void checkReceivedPayloadAndRest() {
            StringBuffer finalPayload = receivedPayload.get();
            String received = finalPayload.toString();
            if (!payload.equals(received)) {
                System.err.println("Didn't recieved expected payload\n" 
                        +   "Expected: "+ payload 
                        + "\nActual:    " + received);
            }
            finalPayload.setLength(0);
        }

        @Override
        public boolean ready() {
            System.out.println("current size = " + currentBatchSize.get() + ", payload = " + payloadSize);
            return currentBatchSize.get() < payloadSize;
        }
        
        
        
    }

}