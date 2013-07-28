package com.google.code.jstringserver.server.nio.select;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.channels.Selector;

import com.google.code.jstringserver.server.FreePortFinder;
import com.google.code.jstringserver.server.Server;

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

}
