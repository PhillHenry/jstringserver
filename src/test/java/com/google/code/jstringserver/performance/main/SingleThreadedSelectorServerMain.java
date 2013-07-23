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
import com.google.code.jstringserver.server.exchange.BlockingSocketChannelExchanger;
import com.google.code.jstringserver.server.exchange.NonBlockingSocketChannelExchanger;
import com.google.code.jstringserver.server.exchange.SocketChannelExchanger;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;
import com.google.code.jstringserver.server.nio.ClientChannelListener;
import com.google.code.jstringserver.server.nio.ReadWriteDispatcher;
import com.google.code.jstringserver.server.nio.select.AbstractSelectionStrategy;
import com.google.code.jstringserver.server.nio.select.NioReader;
import com.google.code.jstringserver.server.nio.select.NioWriter;
import com.google.code.jstringserver.server.nio.select.SingleThreadedSelectionStrategy;
import com.google.code.jstringserver.server.wait.SleepWaitStrategy;

public class SingleThreadedSelectorServerMain {

    public static final int     PORT                = 8888;
    public static final String  EXPECTED_PAYLOAD    = getPayload();

    public static void main(String[] args) throws IOException, InterruptedException {
        SingleThreadedSelectorServerMain app = new SingleThreadedSelectorServerMain();
        app.start(args);
    }
    
    protected void start(String[] args) throws UnknownHostException, IOException {
        String                  ipInterface             = args.length < 1 ? "localhost" : args[0];
        Server                  server                  = getConnectedServer(ipInterface);
        SocketChannelExchanger  socketChannelExchanger  = new NonBlockingSocketChannelExchanger();
        ClientDataHandler       clientDataHandler       = new AsynchClientDataHandler(EXPECTED_PAYLOAD);
        ByteBufferStore         byteBufferStore         = createByteBufferStore();
        
        AbstractSelectionStrategy selectionStrategy     = createSelectionStrategy(clientDataHandler, byteBufferStore);
        ClientChannelListener   clientChannelListener   = new ReadWriteDispatcher(socketChannelExchanger, selectionStrategy);
        SelectorStrategy        selectorStrategy        = new SelectorStrategy(
                server, 
                availableProcessors(), 
                socketChannelExchanger, 
                new SleepWaitStrategy(10), 
                clientChannelListener);
        selectorStrategy.start();
    }

    protected int availableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

    protected AbstractSelectionStrategy createSelectionStrategy(ClientDataHandler clientDataHandler, ByteBufferStore byteBufferStore) {
        return new SingleThreadedSelectionStrategy(
                null, 
                null, 
                new NioWriter(clientDataHandler), 
                new NioReader(clientDataHandler, byteBufferStore));
    }

    private ByteBufferStore createByteBufferStore() {
        ByteBufferFactory   byteBufferFactory   = new DirectByteBufferFactory(4096);
        ByteBufferStore     byteBufferStore     = new ThreadLocalByteBufferStore(byteBufferFactory);
        return byteBufferStore;
    }

    private Server getConnectedServer(String ipInterface) throws UnknownHostException, IOException {
        int backlog = 100;
        Server server = new Server(ipInterface, PORT, true, backlog);
        server.connect();
        return server;
    }

}
