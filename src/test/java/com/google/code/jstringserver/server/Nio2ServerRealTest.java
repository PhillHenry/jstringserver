package com.google.code.jstringserver.server;

import static com.google.code.jstringserver.client.Networker.checkFinished;
import static com.google.code.jstringserver.client.Networker.checkNotInError;
import static com.google.code.jstringserver.performance.AbstractThreadStrategyTest.getPayload;

import org.junit.Test;

import com.google.code.jstringserver.client.Connector;
import com.google.code.jstringserver.performance.AsynchClientDataHandler;
import com.google.code.jstringserver.server.aio.AcceptCompletionHandler;
import com.google.code.jstringserver.server.bytebuffers.factories.ByteBufferFactory;
import com.google.code.jstringserver.server.bytebuffers.factories.DirectByteBufferFactory;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;

public class Nio2ServerRealTest extends AbstractNio2ServerTest  {

    @Test
    public void testReal() throws Exception {
        ByteBufferFactory byteBufferFactory = new DirectByteBufferFactory(1024);
        ClientDataHandler clientDataHandler = new AsynchClientDataHandler(getPayload());
        Nio2Server server = nio2ServerBuilder.getServer();
        AcceptCompletionHandler handler = new AcceptCompletionHandler(byteBufferFactory , clientDataHandler, server.getServerSocketChannel());
        server.register(handler);
        
        Connector[] connectors = startConnectors();
        checkFinished(connectors);
        checkNotInError(connectors);
    }
    
}
