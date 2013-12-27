package com.google.code.jstringserver.performance;

import java.io.IOException;

import org.junit.Ignore;

import com.google.code.jstringserver.server.BatchAcceptorAndReadingThreadStrategy;
import com.google.code.jstringserver.server.Server;
import com.google.code.jstringserver.server.ThreadPoolFactory;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;
import com.google.code.jstringserver.server.nio.select.AbstractNioReader;
import com.google.code.jstringserver.server.nio.select.AbstractNioWriter;
import com.google.code.jstringserver.server.nio.select.ChunkedReaderWriter;
import com.google.code.jstringserver.server.nio.select.NioReader;
import com.google.code.jstringserver.server.nio.select.NioReaderLooping;
import com.google.code.jstringserver.server.nio.select.NioWriter;
import com.google.code.jstringserver.server.nio.select.ReaderWriter;
import com.google.code.jstringserver.server.nio.select.ReaderWriterFactory;
import com.google.code.jstringserver.server.wait.SleepWaitStrategy;

public class BatchingServerAndReadingStrategyTest extends AbstractThreadStrategyTest<BatchAcceptorAndReadingThreadStrategy> {

    @Override
    protected BatchAcceptorAndReadingThreadStrategy threadingStrategy(Server server, ClientDataHandler clientDataHandler)
        throws IOException {
        ThreadPoolFactory       threadPoolFactory   = new ThreadPoolFactory(availableProcessors());
        final AbstractNioReader reader              = new NioReader(
            clientDataHandler, 
            getByteBufferStore(), 
            null);
        final AbstractNioWriter writer              = new NioWriter(clientDataHandler, null);
        
        ReaderWriterFactory     readerWriterFactory = new ReaderWriterFactory() {

            @Override
            public ReaderWriter createReaderWriter() {
                return new ChunkedReaderWriter((NioReader) reader, writer, null);
            }
            
        };
        
        return new BatchAcceptorAndReadingThreadStrategy(
            server,
            readerWriterFactory, 
            threadPoolFactory, 
            null);
    }

    @Override
    protected void checkThreadStrategy(BatchAcceptorAndReadingThreadStrategy threadStrategy) {
        // TODO Auto-generated method stub
    }

    @Override
    protected ClientDataHandler createClientDataHandler() {
        return new AsynchClientDataHandler(payload);
    }


}
