package com.google.code.jstringserver.performance.main;

import static com.google.code.jstringserver.performance.AbstractThreadStrategyTest.getPayload;

import java.io.IOException;
import java.net.UnknownHostException;

import com.google.code.jstringserver.performance.AsynchClientDataHandler;
import com.google.code.jstringserver.server.SelectorStrategy;
import com.google.code.jstringserver.server.Server;
import com.google.code.jstringserver.server.bytebuffers.factories.ByteBufferFactory;
import com.google.code.jstringserver.server.bytebuffers.factories.DirectByteBufferFactory;
import com.google.code.jstringserver.server.bytebuffers.store.ByteBufferStore;
import com.google.code.jstringserver.server.bytebuffers.store.ThreadLocalByteBufferStore;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;
import com.google.code.jstringserver.server.nio.BlockingSocketChannelExchanger;
import com.google.code.jstringserver.server.nio.ClientChannelListener;
import com.google.code.jstringserver.server.nio.NonBlockingSocketChannelExchanger;
import com.google.code.jstringserver.server.nio.ReadWriteDispatcher;
import com.google.code.jstringserver.server.nio.SocketChannelExchanger;
import com.google.code.jstringserver.server.wait.SleepWaitStrategy;

public class SelectorServerMain {

    public static final int     PORT                = 8888;
    public static final String  EXPECTED_PAYLOAD    = getPayload();

    public static void main(String[] args) throws IOException, InterruptedException {
        String                  ipInterface             = args.length < 1 ? "localhost" : args[0];
        Server                  server                  = getConnectedServer(ipInterface);
        SocketChannelExchanger  socketChannelExchanger  = new NonBlockingSocketChannelExchanger();
        ClientDataHandler       clientDataHandler       = new AsynchClientDataHandler(EXPECTED_PAYLOAD);
        ByteBufferStore         byteBufferStore         = createByteBufferStore();
        
        ClientChannelListener   clientChannelListener   = new ReadWriteDispatcher(clientDataHandler ,
                byteBufferStore,
                socketChannelExchanger);
        SelectorStrategy        selectorStrategy        = new SelectorStrategy(
                server, 
                Runtime.getRuntime().availableProcessors(), 
                socketChannelExchanger, 
                new SleepWaitStrategy(10), 
                clientChannelListener);
        selectorStrategy.start();
    }

    private static ByteBufferStore createByteBufferStore() {
        ByteBufferFactory   byteBufferFactory   = new DirectByteBufferFactory(4096);
        ByteBufferStore     byteBufferStore     = new ThreadLocalByteBufferStore(byteBufferFactory);
        return byteBufferStore;
    }

    private static Server getConnectedServer(String ipInterface) throws UnknownHostException, IOException {
        int backlog = 100;
        Server server = new Server(ipInterface, PORT, true, backlog);
        server.connect();
        return server;
    }

}
