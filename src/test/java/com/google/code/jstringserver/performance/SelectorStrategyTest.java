package com.google.code.jstringserver.performance;

import java.io.IOException;

import org.junit.Ignore;

import com.google.code.jstringserver.server.SelectorStrategy;
import com.google.code.jstringserver.server.Server;
import com.google.code.jstringserver.server.bytebuffers.store.ThreadLocalByteBufferStore;
import com.google.code.jstringserver.server.exchange.BlockingSocketChannelExchanger;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;
import com.google.code.jstringserver.server.handlers.ClientReader;
import com.google.code.jstringserver.server.nio.ClientChannelListener;
import com.google.code.jstringserver.server.nio.ReadWriteDispatcher;
import com.google.code.jstringserver.server.nio.select.AbstractNioReader;
import com.google.code.jstringserver.server.nio.select.AbstractNioWriter;
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
        BlockingSocketChannelExchanger  socketChannelExchanger  = new BlockingSocketChannelExchanger();
        AbstractSelectionStrategy       selectionStrategy       = createSelectionStrategy(clientDataHandler);
        ClientChannelListener           clientChannelListener   = new ReadWriteDispatcher(socketChannelExchanger, selectionStrategy);
        return new SelectorStrategy(server, 8, socketChannelExchanger, new SleepWaitStrategy(10), clientChannelListener);
    }

    protected AbstractSelectionStrategy createSelectionStrategy(ClientDataHandler clientDataHandler) {
        AbstractNioWriter writer = new NioWriter(clientDataHandler);
        AbstractNioReader reader = createNioReader(clientDataHandler);
        return createSelectionStrategy(writer, reader);
    }

    protected AbstractNioReader createNioReader(ClientDataHandler clientDataHandler) {
        return new NioReader(clientDataHandler, getByteBufferStore());
    }

    protected AbstractSelectionStrategy createSelectionStrategy(AbstractNioWriter writer, AbstractNioReader reader) {
        return new SingleThreadedSelectionStrategy(
            null, 
            null, 
            writer, 
            reader);
    }

    @Override
    protected void checkThreadStrategy(SelectorStrategy threadStrategy) {
        // TODO Auto-generated method stub
    }

}
