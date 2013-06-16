package com.google.code.jstringserver.performance;

import static com.google.code.jstringserver.server.WritingConnector.createWritingConnectors;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.code.jstringserver.server.Connector;
import com.google.code.jstringserver.server.FreePortFinder;
import com.google.code.jstringserver.server.OneThreadPerClient;
import com.google.code.jstringserver.server.Server;
import com.google.code.jstringserver.server.ThreadStrategy;
import com.google.code.jstringserver.server.ThreadedTaskBuilder;
import com.google.code.jstringserver.server.WritingConnector;
import com.google.code.jstringserver.server.bytebuffers.factories.ByteBufferFactory;
import com.google.code.jstringserver.server.bytebuffers.factories.DirectByteBufferFactory;
import com.google.code.jstringserver.server.bytebuffers.store.ByteBufferStore;
import com.google.code.jstringserver.server.bytebuffers.store.ThreadLocalByteBufferStore;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;
import com.google.code.jstringserver.server.handlers.ClientReader;

public class OneThreadPerClientServer extends AbstractThreadStrategyTest<OneThreadPerClient> {
    
    protected void checkThreadStrategy(OneThreadPerClient threadStrategy) {
        assertEquals(numExpectedCalls(), threadStrategy.numCallsServiced());
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        OneThreadPerClientServer app = new OneThreadPerClientServer();
        app.setUpServer();
        app.shouldProcessAllCalls();
    }
    
    @Override
    protected OneThreadPerClient threadingStrategy(Server server, ClientDataHandler clientDataHandler) {
        ClientReader        clientHandler       = createClientReader(clientDataHandler);
        OneThreadPerClient  oneThreadPerClient  = new OneThreadPerClient(server, 8, clientHandler);
        return oneThreadPerClient;
    }

}
