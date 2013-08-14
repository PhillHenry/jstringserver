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
import com.google.code.jstringserver.server.bytebuffers.factories.DirectByteBufferFactory;
import com.google.code.jstringserver.server.bytebuffers.store.ThreadLocalByteBufferStore;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;

public class BatchServerAndReadingSelectionStrategyTest extends AbstractMultiThreadedTest {
    
    private BatchServerAndReadingSelectionStrategy toTest;
    
    private ServerTestSetup serverTestSetup;

    private ClientTestSetup clientTestSetup;

    @Before
    public void setUp() throws IOException {
        serverTestSetup = new ServerTestSetup();
        clientTestSetup = new ClientTestSetup(serverTestSetup, 1);
    }
    
    @After
    public void shutdown() throws IOException {
        serverTestSetup.shutdown();
    }

    @Test
    public void lifecycle() throws IOException, InterruptedException {
        createToTest();
        toTest.select();
        start(clientTestSetup.createLatchedClients(), "rw");
        toTest.select();
        clientTestSetup.awaitPreWrite();
        toTest.select();
        clientTestSetup.awaitPostWrite();
        toTest.select();
        everythingProcessed();
    }
    
    private ReadWriteMockFacade createToTest() {
        ReadWriteMockFacade mocks = new ReadWriteMockFacade();
        ClientDataHandler mockClientDataHandler = mock(ClientDataHandler.class);
        final NioReader reader = new NioReader(mockClientDataHandler , new ThreadLocalByteBufferStore(new DirectByteBufferFactory(4096)), null);
        Mockito.when(mockClientDataHandler.end(Mockito.any())).thenReturn("OK");
        final AbstractNioWriter writer = new NioWriter(mockClientDataHandler, null);
        ReaderWriterFactory readerWriterFactory = new ReaderWriterFactory() {

            @Override
            public ReaderWriter createReaderWriter() {
                return new ChunkedReaderWriter(reader, writer, null);
            }
            
        };
        toTest = new BatchServerAndReadingSelectionStrategy(null, serverTestSetup.getSelector(), readerWriterFactory, null);
        return mocks;
    }

    private void everythingProcessed() throws IOException, InterruptedException {
        Set<SelectionKey> keys = toTest.selected();
        clientTestSetup.awaitPreRead();
        clientTestSetup.awaitPostRead();
        clientTestSetup.awaitPostClose();
        assertEquals(0, keys.size());
    }

}
