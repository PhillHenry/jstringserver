package com.google.code.jstringserver.server;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ServerTest {

    private Server toTest;
    private int    port;
    private String address = "127.0.0.1";

    @Before
    public void setupServer() throws IOException {
        port    = new FreePortFinder().getFreePort();
        assertTrue(port > 0);
        toTest  = new Server(address, port, true);
        toTest.connect();
    }

    @Test
    public void test() throws Exception {
        Acceptor[]  acceptors           = createAcceptors(2);
        Connector[] connectors          = createConnectors(2);
        Thread[]    acceptorThreads     = start(acceptors, "acceptor");
        Thread[]    connectorThreads    = start(connectors, "connector");
        join(acceptorThreads, 1000L);
        checkFinished(connectors);
        checkNotInError(connectors);
        checkNotInError(acceptors);
        checkFinished(acceptors);
    } 
    
    private Connector[] createConnectors(int num) {
        Connector[] connectors = new Connector[num];
        for (int i = 0 ; i < connectors.length ; i++) {
            connectors[i] = new Connector(address, port);
        }
        return connectors;
    }

    private void join(Thread[] threads, long timeout) throws InterruptedException {
        for (int i = 0 ; i < threads.length ; i++) {
            threads[i].join(timeout);
        }
    }

    private Thread[] start(Runnable[] connectors, String name) {
        Thread[] threads = toThreads(connectors, name);
        for (int i = 0 ; i < threads.length ; i++) {
            threads[i].start();
        }
        return threads;
    }

    private Thread[] toThreads(Runnable[] connectors, String name) {
        Thread[] threads = new Thread[connectors.length];
        for (int i = 0 ; i < connectors.length ; i++) {
            threads[i] = new Thread(connectors[i], name + i);
        }
        return threads;
    }

    private void checkFinished(Networker[] networkTasks) {
        for (Networker task : networkTasks) {
            assertTrue(task.isFinished());
        }
    }

    private void checkNotInError(Networker[] networkTasks) {
        for (Networker task : networkTasks) {
            assertFalse(task.isError());
        }
    }

    private Acceptor[] createAcceptors(int numConnectors) {
        Acceptor[] connectors = new Acceptor[numConnectors];
        for (int i = 0 ; i < numConnectors ; i++) {
            connectors[i] = new Acceptor(toTest);
        }
        return connectors;
    }

}
