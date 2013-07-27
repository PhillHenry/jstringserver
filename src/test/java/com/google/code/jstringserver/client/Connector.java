package com.google.code.jstringserver.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SocketChannel;



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
        
        SocketChannel       socketChannel       = SocketChannel.open();
        try {
            configure(
                inetSocketAddress,
                socketChannel);
            connected(socketChannel);
        } finally {
            socketChannel.close();
        }
    }

    protected void configure(
        InetSocketAddress   inetSocketAddress,
        SocketChannel       socketChannel) throws IOException {
        socketChannel.configureBlocking(true);
        socketChannel.setOption(
            StandardSocketOptions.SO_RCVBUF,
            1000000);
        socketChannel.connect(inetSocketAddress);
    }

    protected void connected(SocketChannel socketChannel) throws IOException {
    }

    public static Connector[] createConnectors(int num, String address, int port) {
        Connector[] connectors = new Connector[num];
        for (int i = 0 ; i < connectors.length ; i++) {
            connectors[i] = new Connector(address, port);
        }
        return connectors;
    }

    @Override
    public String toString() {
        return "Connector [address=" + address + ", port=" + port + ", isError()=" + isError() + ", isFinished()=" + isFinished() + "]";
    }
    
}