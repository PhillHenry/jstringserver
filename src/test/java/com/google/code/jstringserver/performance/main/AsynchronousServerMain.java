package com.google.code.jstringserver.performance.main;

import static com.google.code.jstringserver.performance.AbstractThreadStrategyTest.getPayload;
import static com.google.code.jstringserver.performance.main.AbstractServerMain.PORT;

import com.google.code.jstringserver.performance.AsynchClientDataHandler;
import com.google.code.jstringserver.server.Nio2Server;
import com.google.code.jstringserver.server.aio.AcceptCompletionHandler;
import com.google.code.jstringserver.server.bytebuffers.factories.ByteBufferFactory;
import com.google.code.jstringserver.server.bytebuffers.factories.DirectByteBufferFactory;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;

public class AsynchronousServerMain {

    public static void main(String[] args) throws Exception {
        String              ipInterface         = args.length < 1 ? "localhost" : args[0];
        Nio2Server          server              = new Nio2Server(ipInterface, PORT, 100);
        ByteBufferFactory   byteBufferFactory   = new DirectByteBufferFactory(1024);
        ClientDataHandler   clientDataHandler   = new AsynchClientDataHandler(getPayload());
        AcceptCompletionHandler handler 
            = new AcceptCompletionHandler(byteBufferFactory , clientDataHandler, server.getServerSocketChannel());
        
        server.connect();
        server.register(handler);
        
        Thread.sleep(Long.MAX_VALUE);
    }

}
