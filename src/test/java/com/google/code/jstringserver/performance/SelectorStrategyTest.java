package com.google.code.jstringserver.performance;

import java.io.IOException;

import org.junit.Ignore;

import com.google.code.jstringserver.server.SelectorStrategy;
import com.google.code.jstringserver.server.Server;
import com.google.code.jstringserver.server.bytebuffers.store.ThreadLocalByteBufferStore;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;
import com.google.code.jstringserver.server.handlers.ClientReader;
import com.google.code.jstringserver.server.nio.BlockingSocketChannelExchanger;
import com.google.code.jstringserver.server.nio.ClientChannelListener;
import com.google.code.jstringserver.server.nio.ReadWriteDispatcher;
import com.google.code.jstringserver.server.nio.select.AbstractSelectionStrategy;
import com.google.code.jstringserver.server.nio.select.NioReader;
import com.google.code.jstringserver.server.nio.select.NioWriter;
import com.google.code.jstringserver.server.nio.select.SingleThreadedSelectionStrategy;
import com.google.code.jstringserver.server.wait.SleepWaitStrategy;

public class SelectorStrategyTest extends AbstractThreadStrategyTest<SelectorStrategy> {

    @Override
    protected ClientDataHandler createClientDataHandler() {
        return new AsynchClientDataHandler(payload);
    }

    @Override
    protected SelectorStrategy threadingStrategy(Server server, ClientDataHandler clientDataHandler) throws IOException {
        BlockingSocketChannelExchanger socketChannelExchanger = new BlockingSocketChannelExchanger();
        AbstractSelectionStrategy selectionStrategy = new SingleThreadedSelectionStrategy(
                                                                                          null, 
                                                                                          null, 
                                                                                          new NioWriter(clientDataHandler), 
                                                                                          new NioReader(clientDataHandler, getByteBufferStore()));
        ClientChannelListener clientChannelListener = new ReadWriteDispatcher(socketChannelExchanger, selectionStrategy);
        return new SelectorStrategy(server, 8, socketChannelExchanger, new SleepWaitStrategy(10), clientChannelListener);
    }

    @Override
    protected void checkThreadStrategy(SelectorStrategy threadStrategy) {
        // TODO Auto-generated method stub
    }

}
