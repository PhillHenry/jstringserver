package com.google.code.jstringserver.server;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class ServerBuilder {
    private final String                   address = "localhost";
    private final int                      port;
    private final Server                   server;
    
    public ServerBuilder(int numClients) throws IOException {
        FreePortFinder  freePortFinder  = new FreePortFinder();
        port                            = freePortFinder.getFreePort();
        System.out.println("Server port: " + port);
        server                          = new Server(address, port, true, numClients);
        server.connect();
    }
    
    public void shutdown() throws IOException {
        server.shutdown();
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public Server getServer() {
        return server;
    }
    
    public SocketChannel accept() throws IOException {
        return server.accept();
    }
    
}
