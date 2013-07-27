package com.google.code.jstringserver.performance;

import java.io.IOException;
import java.nio.channels.Selector;

import org.junit.Ignore;

import com.google.code.jstringserver.server.PluggableThreadStrategy;
import com.google.code.jstringserver.server.Server;
import com.google.code.jstringserver.server.bytebuffers.store.ThreadLocalByteBufferStore;
import com.google.code.jstringserver.server.exchange.BlockingSocketChannelExchanger;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;
import com.google.code.jstringserver.server.handlers.ClientReader;
import com.google.code.jstringserver.server.nio.ClientChannelListener;
import com.google.code.jstringserver.server.nio.ReadWriteDispatcher;
import com.google.code.jstringserver.server.nio.ServerSocketDispatchingSelectionStrategy;
import com.google.code.jstringserver.server.nio.select.AbstractNioReader;
import com.google.code.jstringserver.server.nio.select.AbstractNioWriter;
import com.google.code.jstringserver.server.nio.select.AbstractSelectionStrategy;
import com.google.code.jstringserver.server.nio.select.NioReader;
import com.google.code.jstringserver.server.nio.select.NioWriter;
import com.google.code.jstringserver.server.nio.select.SingleThreadedReadingSelectionStrategy;
import com.google.code.jstringserver.server.wait.SleepWaitStrategy;

public class SelectorStrategyTest extends AbstractThreadStrategyTest<PluggableThreadStrategy> {

    @Override
    protected ClientDataHandler createClientDataHandler() {
        return new AsynchClientDataHandler(payload);
    }

    @Override
    protected PluggableThreadStrategy threadingStrategy(Server server, ClientDataHandler clientDataHandler) throws IOException {
        BlockingSocketChannelExchanger  socketChannelExchanger  = new BlockingSocketChannelExchanger();
        AbstractSelectionStrategy       selectionStrategy       = createSelectionStrategy(clientDataHandler);
        ClientChannelListener           clientChannelListener   = new ReadWriteDispatcher(socketChannelExchanger, selectionStrategy, clientSelector);
        AbstractSelectionStrategy       acceptorStrategy        = createAcceptorStrategy(socketChannelExchanger);
        
        return new PluggableThreadStrategy(server, socketChannelExchanger, new SleepWaitStrategy(10), clientChannelListener, acceptorStrategy);
    }

    protected ServerSocketDispatchingSelectionStrategy createAcceptorStrategy(BlockingSocketChannelExchanger socketChannelExchanger) throws IOException {
        return new ServerSocketDispatchingSelectionStrategy(
            null, 
            serverSelector, 
            socketChannelExchanger);
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
        return new SingleThreadedReadingSelectionStrategy(
            null, 
            clientSelector, 
            writer, 
            reader);
    }

    @Override
    protected void checkThreadStrategy(PluggableThreadStrategy threadStrategy) {
        // TODO Auto-generated method stub
    }

}
