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
    
    private int expectedReadSizeOnFinish;

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
        MyChunkedReaderWriter readerWriter = doClientsReadWrite();
        toTest.select(); // client has finished writing. 
        everythingProcessed(readerWriter);
    }
    
    @Test
    public void clientClosesBeforeFinish() throws IOException, InterruptedException {
        MyChunkedReaderWriter readerWriter = doClientsReadWrite();
        readerWriter.writeToEveryone();
        clientTestSetup.awaitPostClose();
        expectedReadSizeOnFinish = -1;
        everythingProcessed(readerWriter);
    }
    
    private MyChunkedReaderWriter doClientsReadWrite() throws IOException, InterruptedException {
        MyChunkedReaderWriter readerWriter = createToTest();
        toTest.select();
        start(clientTestSetup.createLatchedClients(PAYLOAD), "rw");
        toTest.select();
        clientTestSetup.awaitPreWrite();
        toTest.select();
        clientTestSetup.awaitPostWrite();
        assertEquals(1, readerWriter.allSelectionKeysEver.size());
        
        return readerWriter;
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
        protected void finishedReading(SelectionKey key, SocketChannel selectableChannel) throws IOException {
            assertEquals(expectedReadSizeOnFinish, selectableChannel.read(ByteBuffer.allocateDirect(1024)));
            super.finishedReading(key, selectableChannel);
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
        
        public void writeToEveryone() throws IOException {
            for (SelectionKey key : allSelectionKeysEver) {
                write(key, (SocketChannel) key.channel());
            }
        }
    }

    private void everythingProcessed(MyChunkedReaderWriter readerWriter) throws IOException, InterruptedException {
        Set<SelectionKey> keys = toTest.selected();
        clientTestSetup.awaitPreRead();
        clientTestSetup.awaitPostRead();
        clientTestSetup.awaitPostClose();
        assertEquals(0, keys.size());
        assertEquals(PAYLOAD.getBytes().length, readerWriter.bytesRead);
    }

}
