package com.google.code.jstringserver.server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;


public class Connector extends Networker {
    
    private final String address;
    private final int port;

    public Connector(String address, int port) {
        super();
        this.address = address;
        this.port = port;
    }

    @Override
    protected void doCall() throws Exception {
        InetAddress         addr                = InetAddress.getByName(address);
        InetSocketAddress   inetSocketAddress   = new InetSocketAddress(addr, port);
        Socket              socket              = new Socket();
        socket.connect(inetSocketAddress);
        socket.close();
    }

    public static Connector[] createConnectors(int num, String address, int port) {
        Connector[] connectors = new Connector[num];
        for (int i = 0 ; i < connectors.length ; i++) {
            connectors[i] = new Connector(address, port);
        }
        return connectors;
    }
    
}