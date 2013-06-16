package com.google.code.jstringserver.server;

import static com.google.code.jstringserver.server.Networker.checkFinished;
import static com.google.code.jstringserver.server.Networker.checkNotInError;
import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ServerTest extends AbstractMultiThreadedTest {

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
        Acceptor[]  acceptors           = Acceptor.createAcceptors(2, toTest);
        Connector[] connectors          = Connector.createConnectors(2, address, port);
        Thread[]    acceptorThreads     = start(acceptors, "acceptor");
        Thread[]    connectorThreads    = start(connectors, "connector");
        join(acceptorThreads, 1000L);
        checkFinished(connectors);
        checkNotInError(connectors);
        checkNotInError(acceptors);
        checkFinished(acceptors);
    }

}
