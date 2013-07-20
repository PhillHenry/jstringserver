package com.google.code.jstringserver.server;

import static com.google.code.jstringserver.client.Connector.createConnectors;
import static com.google.code.jstringserver.client.Networker.checkFinished;
import static com.google.code.jstringserver.client.Networker.checkInError;
import static com.google.code.jstringserver.client.Networker.checkNotInError;
import static com.google.code.jstringserver.server.Acceptor.createAcceptors;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.code.jstringserver.client.BlockingConnector;
import com.google.code.jstringserver.client.Connector;

public class ServerTest extends AbstractMultiThreadedTest {

    private static final int READ_TIMEOUT_MS = 100;
    private Server toTest;
    private int    port;
    private String address = "127.0.0.1";

    @Before
    public void setupServer() throws IOException {
        port    = new FreePortFinder().getFreePort();
        assertTrue(port > 0);
        toTest  = new Server(address, port, true, 100);
        toTest.connect();
    }

    @Test
    public void smokeTest() throws Exception {
        Acceptor[]  acceptors           = createAcceptors(2, toTest);
        Connector[] connectors          = createConnectors(2, address, port);
        Thread[]    acceptorThreads     = start(acceptors, "acceptor");
        Thread[]    connectorThreads    = start(connectors, "connector");
        join(acceptorThreads, 1000L);
        checkFinished(connectors);
        checkNotInError(connectors);
        checkNotInError(acceptors);
        checkFinished(acceptors);
    }
    
    @Test 
    @Ignore // this ends up testing the test code as the (test) acceptor code configures the socket to timeout
    public void readTimeOut() throws Exception {
        Acceptor            acceptor            = new ReadWriteAcceptor(toTest);
        Thread              acceptorThread      = new Thread(acceptor, "acceptor");
        acceptorThread.start();
        BlockingConnector   blockingConnector   = new BlockingConnector(address, port);
        Thread              blockingThread      = new Thread(blockingConnector, "blocking thread");
        blockingThread.start();
        acceptorThread.join(1000L);
        
        assertTrue(acceptor.isError());
        assertTrue(acceptor.isFinished());
        assertTrue(acceptor.getException() instanceof SocketTimeoutException);
        
        blockingThread.interrupt();
    }

}
