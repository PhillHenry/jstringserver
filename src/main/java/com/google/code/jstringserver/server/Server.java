package com.google.code.jstringserver.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Server {

    private final InetAddress   listeningAddress;
    private final int           port;
    private ServerSocketChannel serverSocketChannel;

    Server(String address, int port) throws UnknownHostException {
        listeningAddress = InetAddress.getByName(address);
        this.port = port;
    }

    void connect() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        SocketAddress socketAddress = new InetSocketAddress(listeningAddress, port);
        serverSocketChannel.bind(socketAddress);
    }

    SocketChannel accept() throws IOException {
        return serverSocketChannel.accept();
    }

}
