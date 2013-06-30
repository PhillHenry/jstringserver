package com.google.code.jstringserver.performance;

import java.io.IOException;

import org.junit.Ignore;

import com.google.code.jstringserver.server.SelectorStrategy;
import com.google.code.jstringserver.server.Server;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;
import com.google.code.jstringserver.server.nio.BlockingSocketChannelExchanger;
import com.google.code.jstringserver.server.wait.SleepWaitStrategy;

@Ignore
public class SelectorStrategyTest extends AbstractThreadStrategyTest<SelectorStrategy> {

    @Override
    protected SelectorStrategy threadingStrategy(Server server, ClientDataHandler clientDataHandler) throws IOException {
        return new SelectorStrategy(
                server,
                8,
                clientDataHandler,
                getByteBufferStore(),
                new BlockingSocketChannelExchanger(),
                new SleepWaitStrategy(10));
    }

    @Override
    protected void checkThreadStrategy(SelectorStrategy threadStrategy) {
        // TODO Auto-generated method stub
    }

}
