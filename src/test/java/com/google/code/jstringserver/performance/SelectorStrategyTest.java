package com.google.code.jstringserver.performance;

import java.io.IOException;

import com.google.code.jstringserver.server.ExchangingThreadStrategy;
import com.google.code.jstringserver.server.Server;
import com.google.code.jstringserver.server.exchange.BlockingSocketChannelExchanger;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;
import com.google.code.jstringserver.server.nio.ClientChannelListener;
import com.google.code.jstringserver.server.nio.ReadWriteDispatcher;
import com.google.code.jstringserver.server.nio.ServerSocketDispatchingSelectionStrategy;
import com.google.code.jstringserver.server.nio.select.AbstractNioReader;
import com.google.code.jstringserver.server.nio.select.AbstractNioWriter;
import com.google.code.jstringserver.server.nio.select.AbstractSelectionStrategy;
import com.google.code.jstringserver.server.nio.select.NioReader;
import com.google.code.jstringserver.server.nio.select.NioWriter;
import com.google.code.jstringserver.server.nio.select.SelectionStrategy;
import com.google.code.jstringserver.server.nio.select.SingleThreadedReadingSelectionStrategy;
import com.google.code.jstringserver.server.wait.SleepWaitStrategy;

public class SelectorStrategyTest extends AbstractThreadStrategyTest<ExchangingThreadStrategy> {

    @Override
    protected ClientDataHandler createClientDataHandler() {
        return new AsynchClientDataHandler(payload);
    }

    @Override
    protected ExchangingThreadStrategy threadingStrategy(Server server, ClientDataHandler clientDataHandler) throws IOException {
        BlockingSocketChannelExchanger  socketChannelExchanger  = new BlockingSocketChannelExchanger();
        SelectionStrategy       selectionStrategy       = createSelectionStrategy(clientDataHandler);
        ClientChannelListener           clientChannelListener   = new ReadWriteDispatcher(socketChannelExchanger, selectionStrategy, clientSelector);
        AbstractSelectionStrategy       acceptorStrategy        = createAcceptorStrategy(socketChannelExchanger);
        
        return new ExchangingThreadStrategy(server, socketChannelExchanger, new SleepWaitStrategy(10), clientChannelListener, acceptorStrategy);
    }

    protected ServerSocketDispatchingSelectionStrategy createAcceptorStrategy(BlockingSocketChannelExchanger socketChannelExchanger) throws IOException {
        return new ServerSocketDispatchingSelectionStrategy(
            null, 
            serverSelector, 
            socketChannelExchanger);
    }

    protected SelectionStrategy createSelectionStrategy(ClientDataHandler clientDataHandler) {
        AbstractNioWriter writer = new NioWriter(clientDataHandler);
        AbstractNioReader reader = createNioReader(clientDataHandler);
        return createSelectionStrategy(writer, reader);
    }

    protected AbstractNioReader createNioReader(ClientDataHandler clientDataHandler) {
        return new NioReader(clientDataHandler, getByteBufferStore());
    }

    protected SelectionStrategy createSelectionStrategy(AbstractNioWriter writer, AbstractNioReader reader) {
        return new SingleThreadedReadingSelectionStrategy(
            null, 
            clientSelector, 
            writer, 
            reader);
    }

    @Override
    protected void checkThreadStrategy(ExchangingThreadStrategy threadStrategy) {
        // TODO Auto-generated method stub
    }

}
