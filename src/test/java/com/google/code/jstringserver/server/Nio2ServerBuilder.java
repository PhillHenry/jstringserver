package com.google.code.jstringserver.server;


public class Nio2ServerBuilder {
    public static final String address = "127.0.0.1";
    
    private Nio2Server      server;
    private int             port;

    public void setUp() throws Exception {
        port    = new FreePortFinder().getFreePort();
        server  = new Nio2Server(address, port, 100);
        server.connect();
    }
    
    public void tearDown() throws Exception {
        if (server != null) {
            server.close();
        }
    }

    protected Nio2Server getServer() {
        return server;
    }

    protected void setServer(Nio2Server server) {
        this.server = server;
    }

    protected int getPort() {
        return port;
    }

    protected void setPort(int port) {
        this.port = port;
    }

    protected String getAddress() {
        return address;
    }
}
