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
import com.google.code.jstringserver.server.nio.SingleThreadedClientChannelListener;
import com.google.code.jstringserver.server.wait.SleepWaitStrategy;

@Ignore
public class SelectorStrategyTest extends AbstractThreadStrategyTest<SelectorStrategy> {

    @Override
    protected ClientDataHandler createClientDataHandler() {
        return new AsynchClientDataHandler(payload);
    }

    @Override
    protected SelectorStrategy threadingStrategy(Server server, ClientDataHandler clientDataHandler) throws IOException {
        BlockingSocketChannelExchanger socketChannelExchanger = new BlockingSocketChannelExchanger();
        ClientChannelListener clientChannelListener = new SingleThreadedClientChannelListener(clientDataHandler,
                                                                                              getByteBufferStore(),
                                                                                              socketChannelExchanger);
        return new SelectorStrategy(server, 8, socketChannelExchanger, new SleepWaitStrategy(10), clientChannelListener);
    }

    @Override
    protected void checkThreadStrategy(SelectorStrategy threadStrategy) {
        // TODO Auto-generated method stub
    }

}
