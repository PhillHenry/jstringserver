package com.google.code.jstringserver.client;

import static java.net.StandardSocketOptions.SO_RCVBUF;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SocketChannel;

import com.google.code.jstringserver.stats.Stopwatch;



public class Connector extends Networker {
    
    private final String address;
    private final int port;
    private final Stopwatch connectStopWatch;

    public Connector(String address, int port, Stopwatch connectStopWatch) {
        super();
        this.address = address;
        this.port = port;
        this.connectStopWatch = connectStopWatch;
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
            close(socketChannel);
        }
    }

    protected void close(SocketChannel socketChannel) throws IOException {
        socketChannel.socket().close();
        socketChannel.close();
    }

    protected void configure(
        InetSocketAddress   inetSocketAddress,
        SocketChannel       socketChannel) throws IOException {
        socketChannel.configureBlocking(true);
        socketChannel.socket().setSoLinger(true, 0);
        socketChannel.setOption(
            SO_RCVBUF,
            1000000);
        startConnectStopWatch();
        try {
            socketChannel.connect(inetSocketAddress);
        } finally {
            stopConnectStopWatch();
        }
    }

    private void stopConnectStopWatch() {
        if (connectStopWatch != null) {
            connectStopWatch.stop();
        }
    }

    private void startConnectStopWatch() {
        if (connectStopWatch != null) {
            connectStopWatch.start();
        }
    }

    protected void connected(SocketChannel socketChannel) throws IOException {
    }

    public static Connector[] createConnectors(int num, String address, int port) {
        Connector[] connectors = new Connector[num];
        for (int i = 0 ; i < connectors.length ; i++) {
            connectors[i] = new Connector(address, port, null);
        }
        return connectors;
    }

    @Override
    public String toString() {
        return "Connector [address=" + address + ", port=" + port + ", isError()=" + isError() + ", isFinished()=" + isFinished() + "]";
    }
    
}