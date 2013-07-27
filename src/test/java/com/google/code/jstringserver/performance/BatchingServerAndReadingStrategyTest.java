package com.google.code.jstringserver.performance;

import java.io.IOException;

import org.junit.Ignore;

import com.google.code.jstringserver.server.BatchAcceptorAndReadingThreadStrategy;
import com.google.code.jstringserver.server.Server;
import com.google.code.jstringserver.server.ThreadPoolFactory;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;
import com.google.code.jstringserver.server.nio.select.AbstractNioReader;
import com.google.code.jstringserver.server.nio.select.NioReaderLooping;
import com.google.code.jstringserver.server.wait.SleepWaitStrategy;

@Ignore // work in progress
public class BatchingServerAndReadingStrategyTest extends AbstractThreadStrategyTest<BatchAcceptorAndReadingThreadStrategy> {

    @Override
    protected BatchAcceptorAndReadingThreadStrategy threadingStrategy(Server server, ClientDataHandler clientDataHandler)
        throws IOException {
        ThreadPoolFactory threadPoolFactory = new ThreadPoolFactory(availableProcessors());
        AbstractNioReader reader = new NioReaderLooping(clientDataHandler, getByteBufferStore(), 1000, new SleepWaitStrategy(10));
        return new BatchAcceptorAndReadingThreadStrategy(reader , threadPoolFactory);
    }

    @Override
    protected void checkThreadStrategy(BatchAcceptorAndReadingThreadStrategy threadStrategy) {
        // TODO Auto-generated method stub
    }


}
