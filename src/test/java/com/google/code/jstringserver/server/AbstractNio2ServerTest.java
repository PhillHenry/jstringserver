package com.google.code.jstringserver.server;

import static com.google.code.jstringserver.client.Connector.createConnectors;

import org.junit.After;
import org.junit.Before;

import com.google.code.jstringserver.client.Connector;

public class AbstractNio2ServerTest extends AbstractMultiThreadedTest {
    
    protected Nio2ServerBuilder   nio2ServerBuilder;

    @Before
    public void setUp() throws Exception {
        nio2ServerBuilder = new Nio2ServerBuilder();
        nio2ServerBuilder.setUp();
    }
    
    @After
    public void tearDown() throws Exception {
        if (nio2ServerBuilder != null) {
            nio2ServerBuilder.tearDown();
        }
    }

    protected Connector[] startConnectors() throws InterruptedException {
        Connector[] connectors          = createConnectors(1, nio2ServerBuilder.getAddress(), nio2ServerBuilder.getPort());
        Thread[]    acceptorThreads     = start(connectors, "connector");
        join(acceptorThreads, 1000L);
        return connectors;
    }
}
