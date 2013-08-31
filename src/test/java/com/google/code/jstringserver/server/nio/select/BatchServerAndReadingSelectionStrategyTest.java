package com.google.code.jstringserver.server.nio.select;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.code.jstringserver.client.LatchedWritingConnector;
import com.google.code.jstringserver.server.AbstractMultiThreadedTest;
import com.google.code.jstringserver.server.bytebuffers.factories.DirectByteBufferFactory;
import com.google.code.jstringserver.server.bytebuffers.store.ThreadLocalByteBufferStore;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;
import com.google.code.jstringserver.stats.Stopwatch;

public class BatchServerAndReadingSelectionStrategyTest extends AbstractMultiThreadedTest {
    
    private static final String PAYLOAD = "this is the payload";

    private BatchServerAndReadingSelectionStrategy toTest;
    
    private ServerTestSetup serverTestSetup;

    private ClientTestSetup clientTestSetup;

    private ClientDataHandler mockClientDataHandler;
    
    private LatchedWritingConnector[] clients;

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
        MyChunkedReaderWriter readerWriter = awaitClientsWrite();
        toTest.select(); // client has finished writing. This will trigger a write to the client and thus make it read
        everythingProcessed(readerWriter);
    }
    
    @Test
    public void clientClosesBeforeServerFinishes() throws IOException, InterruptedException {
        MyChunkedReaderWriter readerWriter = createToTest();
        startClients();
        clientTestSetup.awaitPostWrite(); // client is now waiting for a response
        toTest.select(); // reads from client. state ready to write
        toTest.select(); // triggers write to client
        assertEquals(1, readerWriter.allSelectionKeysEver.size());
    }
    
    @Test
    public void clientPrematurelyCloses() throws InterruptedException, IOException {
        MyChunkedReaderWriter readerWriter = awaitClientsWrite();
        closeAllClients();
        assertEquals(PAYLOAD.getBytes().length, readerWriter.bytesRead);
        SelectionKey selectionKey = readerWriter.allSelectionKeysEver.iterator().next();
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        checkNoLongerReadable(channel);
    }
    
    private void closeAllClients() throws IOException {
        for (LatchedWritingConnector client : clients) {
            client.close();
        }
    }
    
    private MyChunkedReaderWriter awaitClientsWrite() throws IOException, InterruptedException {
        MyChunkedReaderWriter readerWriter = createToTest();
        toTest.select();
        startClients();
        toTest.select();
        clientTestSetup.awaitPreWrite();
        toTest.select();
        clientTestSetup.awaitPostWrite();
        assertEquals(1, readerWriter.allSelectionKeysEver.size());
        
        return readerWriter;
    }

    private void startClients() {
        clients = clientTestSetup.createLatchedClients(PAYLOAD);
        start(clients, "rw");
    }
   
    private MyChunkedReaderWriter createToTest() {
        mockClientDataHandler = mock(ClientDataHandler.class);
        when(mockClientDataHandler.isNotComplete(Mockito.any())).thenReturn(true);
        when(mockClientDataHandler.end(Mockito.any())).thenReturn("OK");
        return createToTest(mockClientDataHandler);
    }

    private MyChunkedReaderWriter createToTest(ClientDataHandler mockClientDataHandler) {
        final NioReader             reader              = new NioReader(mockClientDataHandler, 
            new ThreadLocalByteBufferStore(new DirectByteBufferFactory(4096)), 
            null);
        final AbstractNioWriter     writer              = new NioWriter(mockClientDataHandler, null);
        final MyChunkedReaderWriter readerWriter        = new MyChunkedReaderWriter(reader, writer, null);
        ReaderWriterFactory         readerWriterFactory = new ReaderWriterFactory() {

            @Override
            public ReaderWriter createReaderWriter() {
                return readerWriter;
            }
            
        };
        toTest = new BatchServerAndReadingSelectionStrategy(null, serverTestSetup.getSelector(), readerWriterFactory, null);
        return readerWriter;
    }
    
    private class MyChunkedReaderWriter extends ChunkedReaderWriter {
        
        private final Set<SelectionKey> allSelectionKeysEver = new HashSet<>();
        
        private int bytesRead;

        public MyChunkedReaderWriter(
            NioReader reader,
            AbstractNioWriter writer,
            Stopwatch stopwatch) {
            super(reader, writer, stopwatch);
        }

        @Override
        public void doWork(SelectionKey key) throws IOException {
            allSelectionKeysEver.add(key);
            super.doWork(key);
        }

        @Override
        protected void close(SelectionKey key, SocketChannel selectableChannel) throws IOException {
            assertNoMoreToRead(selectableChannel);
            super.close(key, selectableChannel);
        }

        private void assertNoMoreToRead(SocketChannel selectableChannel) throws IOException {
            waitForClientToFinish();
            checkNoLongerReadable(selectableChannel);
        }

        @Override
        protected int read(SelectionKey key, SocketChannel selectableChannel) throws IOException {
            int read = super.read(key, selectableChannel);
            bytesRead += read;
            if (bytesRead == PAYLOAD.getBytes().length) {
                when(mockClientDataHandler.isNotComplete(Mockito.any())).thenReturn(false);
            }
            return read;
        }
    }
    
    private void waitForClientToFinish() {
        try {
            clientTestSetup.awaitPostClose();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkNoLongerReadable(SocketChannel selectableChannel) throws IOException {
        int lastRead = selectableChannel.read(ByteBuffer.allocateDirect(1024));
        System.out.println("Server side: finished reading from client. Last read = " + lastRead);
        assertEquals(-1, lastRead);
    }
    
    private void everythingProcessed(MyChunkedReaderWriter readerWriter) throws IOException, InterruptedException {
        Set<SelectionKey> keys = toTest.selected();
        assertEquals(0, keys.size());
        clientTestSetup.awaitPreRead();
        clientTestSetup.awaitPostRead();
        clientTestSetup.awaitPostClose();
        assertEquals(PAYLOAD.getBytes().length, readerWriter.bytesRead);
    }

}
