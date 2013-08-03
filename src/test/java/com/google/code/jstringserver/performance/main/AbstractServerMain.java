package com.google.code.jstringserver.performance.main;

import static com.google.code.jstringserver.performance.AbstractThreadStrategyTest.getPayload;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Map;

import com.google.code.jstringserver.performance.AsynchClientDataHandler;
import com.google.code.jstringserver.server.ExchangingThreadStrategy;
import com.google.code.jstringserver.server.Server;
import com.google.code.jstringserver.server.bytebuffers.factories.ByteBufferFactory;
import com.google.code.jstringserver.server.bytebuffers.factories.DirectByteBufferFactory;
import com.google.code.jstringserver.server.bytebuffers.store.ByteBufferStore;
import com.google.code.jstringserver.server.bytebuffers.store.ThreadLocalByteBufferStore;
import com.google.code.jstringserver.server.exchange.NonBlockingSocketChannelExchanger;
import com.google.code.jstringserver.server.exchange.SocketChannelExchanger;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;
import com.google.code.jstringserver.server.nio.ClientChannelListener;
import com.google.code.jstringserver.server.nio.ReadWriteDispatcher;
import com.google.code.jstringserver.server.nio.ServerSocketDispatchingSelectionStrategy;
import com.google.code.jstringserver.server.nio.select.AbstractSelectionStrategy;
import com.google.code.jstringserver.server.nio.select.SelectionStrategy;
import com.google.code.jstringserver.server.wait.SleepWaitStrategy;
import com.google.code.jstringserver.stats.Stopwatch;
import com.google.code.jstringserver.stats.ThreadLocalStopWatch;

public abstract class AbstractServerMain {

    public static final int     PORT                    = 8888;
    public static final String  EXPECTED_PAYLOAD        = getPayload();
    
    private final StatsCollector statsCollector;
    private final Selector       clientSelector;
    
    public AbstractServerMain() throws IOException {
        super();
        clientSelector = Selector.open();
        statsCollector = new StatsCollector();
    }

    protected void start(String[] args) throws UnknownHostException, IOException, InterruptedException {
        String                  ipInterface             = args.length < 1 ? "localhost" : args[0];
        Server                  server                  = getConnectedServer(ipInterface);
        SocketChannelExchanger  socketChannelExchanger  = new NonBlockingSocketChannelExchanger();
        ClientDataHandler       clientDataHandler       = new AsynchClientDataHandler(EXPECTED_PAYLOAD);
        ByteBufferStore         byteBufferStore         = createByteBufferStore();
        
        SelectionStrategy           selectionStrategy       = createSelectionStrategy(clientDataHandler, byteBufferStore);
        ClientChannelListener       clientChannelListener   = createClientListener(socketChannelExchanger, selectionStrategy);
        SleepWaitStrategy           waitStrategy            = new SleepWaitStrategy(10);
        AbstractSelectionStrategy   acceptorStrategy        = createAcceptorStrategy(socketChannelExchanger, waitStrategy);
        ExchangingThreadStrategy    selectorStrategy        = new ExchangingThreadStrategy(
                server, 
                socketChannelExchanger, 
                waitStrategy, 
                clientChannelListener,
                acceptorStrategy);
        selectorStrategy.start();
        statsCollector.started();
    }


    protected abstract SelectionStrategy createSelectionStrategy(ClientDataHandler clientDataHandler, ByteBufferStore byteBufferStore); 
    
    private ReadWriteDispatcher createClientListener(
        SocketChannelExchanger socketChannelExchanger, 
        SelectionStrategy selectionStrategy)
        throws IOException {
        return new ReadWriteDispatcher(socketChannelExchanger, selectionStrategy, clientSelector);
    }

    protected AbstractSelectionStrategy createAcceptorStrategy(
        SocketChannelExchanger socketChannelExchanger, 
        SleepWaitStrategy waitStrategy) throws IOException {
        Selector serverSelector = Selector.open();
        return new ServerSocketDispatchingSelectionStrategy(
            waitStrategy, 
            serverSelector, 
            socketChannelExchanger);
    }

    protected int availableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

    private ByteBufferStore createByteBufferStore() {
        ByteBufferFactory   byteBufferFactory   = new DirectByteBufferFactory(4096);
        ByteBufferStore     byteBufferStore     = new ThreadLocalByteBufferStore(byteBufferFactory);
        return byteBufferStore;
    }

    public static Server getConnectedServer(String ipInterface) throws UnknownHostException, IOException {
        int backlog = 100;
        Server server = new Server(ipInterface, PORT, true, backlog);
        server.connect();
        return server;
    }


    protected Stopwatch getStopWatchFor(String name) {
        return statsCollector.getStopWatchFor(name);
    }

    protected Selector getClientSelector() {
        return clientSelector;
    }

}
