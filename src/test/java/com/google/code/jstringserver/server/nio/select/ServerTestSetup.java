package com.google.code.jstringserver.server.nio.select;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

import com.google.code.jstringserver.server.FreePortFinder;
import com.google.code.jstringserver.server.Server;
import com.google.code.jstringserver.server.nio.ClientConfigurer;
import com.google.code.jstringserver.server.nio.SimpleSelectorHolder;

public class ServerTestSetup {
    
    public static final String HOST = "localhost";
    private int port;
    private Server server;
    private Selector selector;
    
    public ServerTestSetup() throws IOException {
        super();
        setUp();
    }

    public void setUp() throws IOException {
        startServer();
        startSelector();
    }

    private void startSelector() throws IOException {
        selector = Selector.open();
        server.register(selector);
    }

    private void startServer() throws IOException, UnknownHostException {
        FreePortFinder freePortFinder = new FreePortFinder();
        port = freePortFinder.getFreePort();
        server = new Server(HOST, port, false, 0);
        server.connect();
    }

    public Selector getSelector() {
        return selector;
    }

    public void shutdown() throws IOException {
        server.shutdown();
        selector.close();
    }

    public int getPort() {
        return port;
    }

    public void accept(Selector clientSelector) throws IOException {
        int select = selector.select(1000);
        if (select == 0) {
            fail("Was expecting at least one connection");
        } else {
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            for (SelectionKey key : selectedKeys) {
                ServerSocketChannel selectableChannel = (ServerSocketChannel) key.channel();
                SocketChannel clientChannel = selectableChannel.accept();
                ClientConfigurer clientConfigurer = new ClientConfigurer(new SimpleSelectorHolder(clientSelector));
                clientConfigurer.register(clientChannel);
            }
        }
    }
    
}
