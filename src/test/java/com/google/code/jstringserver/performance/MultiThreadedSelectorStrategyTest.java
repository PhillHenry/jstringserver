package com.google.code.jstringserver.performance;

import org.junit.Ignore;

import com.google.code.jstringserver.server.handlers.ClientDataHandler;
import com.google.code.jstringserver.server.nio.select.AbstractSelectionStrategy;
import com.google.code.jstringserver.server.nio.select.MultiThreadedSelectionStrategy;
import com.google.code.jstringserver.server.nio.select.NioReader;
import com.google.code.jstringserver.server.nio.select.NioReaderLooping;
import com.google.code.jstringserver.server.nio.select.NioWriter;

public class MultiThreadedSelectorStrategyTest extends SelectorStrategyTest {

    @Override
    protected AbstractSelectionStrategy createSelectionStrategy(NioWriter writer, NioReader reader) {
        return new MultiThreadedSelectionStrategy(null, null, writer, reader, Runtime.getRuntime().availableProcessors());
    }

//    @Override
//    protected int getNumberOfClients() {
//        return 1;
//    }

    @Override
    protected NioReader createNioReader(ClientDataHandler clientDataHandler) {
        return new NioReaderLooping(clientDataHandler, getByteBufferStore());
    }

    
    
}
