package com.google.code.jstringserver.performance;

import java.io.IOException;

import org.junit.Ignore;

import com.google.code.jstringserver.server.BatchAcceptorAndReadingThreadStrategy;
import com.google.code.jstringserver.server.Server;
import com.google.code.jstringserver.server.ThreadPoolFactory;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;
import com.google.code.jstringserver.server.nio.select.AbstractNioReader;
import com.google.code.jstringserver.server.nio.select.AbstractNioWriter;
import com.google.code.jstringserver.server.nio.select.NioReader;
import com.google.code.jstringserver.server.nio.select.NioReaderLooping;
import com.google.code.jstringserver.server.nio.select.NioWriter;
import com.google.code.jstringserver.server.wait.SleepWaitStrategy;

public class BatchingServerAndReadingStrategyTest extends AbstractThreadStrategyTest<BatchAcceptorAndReadingThreadStrategy> {

    @Override
    protected BatchAcceptorAndReadingThreadStrategy threadingStrategy(Server server, ClientDataHandler clientDataHandler)
        throws IOException {
        ThreadPoolFactory threadPoolFactory = new ThreadPoolFactory(availableProcessors());
        AbstractNioReader reader            = new NioReader(
            clientDataHandler, 
            getByteBufferStore(), 
            null);
        AbstractNioWriter writer            = new NioWriter(clientDataHandler, null);
        return new BatchAcceptorAndReadingThreadStrategy(
            server,
            reader, 
            threadPoolFactory, 
            writer,
            null, 
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
