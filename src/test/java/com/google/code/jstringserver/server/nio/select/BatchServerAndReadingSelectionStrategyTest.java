package com.google.code.jstringserver.server.nio.select;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.code.jstringserver.server.AbstractMultiThreadedTest;

public class BatchServerAndReadingSelectionStrategyTest extends AbstractMultiThreadedTest {
    
    private BatchServerAndReadingSelectionStrategy toTest;
    
    private ServerTestSetup serverTestSetup;

    private ReadWriteMockFacade mocks;

    @Before
    public void setUp() throws IOException {
        serverTestSetup = new ServerTestSetup();
        mocks = new ReadWriteMockFacade();
        toTest = new BatchServerAndReadingSelectionStrategy(null, serverTestSetup.getSelector(), mocks.getReader(), mocks.getWriter());
    }
    
    @After
    public void shutdown() throws IOException {
        serverTestSetup.shutdown();
    }

    @Test
    public void lifecycle() throws IOException, InterruptedException {
        ClientTestSetup clientTestSetup = new ClientTestSetup(serverTestSetup, 1);
        start(clientTestSetup.createLatchedClients(), "rw");
        clientTestSetup.awaitPostRead();
        clientTestSetup.awaitPreWrite();
        toTest.select();
        toTest.select();
        mocks.checkReadAndWrite(1);
        
        everythingProcessed();
    }

    private void everythingProcessed() throws IOException {
        Set<SelectionKey> keys = toTest.selected();
        assertEquals(0, keys.size());
    }

}
