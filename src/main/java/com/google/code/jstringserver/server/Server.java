package com.google.code.jstringserver.server;

import static java.net.StandardSocketOptions.SO_RCVBUF;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Server {

    private final InetAddress   listeningAddress;
    private final int           port;
    private final boolean       blocking;
    private final int           backlog;
    
    private ServerSocketChannel serverSocketChannel;

    public Server(String address, int port, boolean blockingServer, int backlog) 
            throws UnknownHostException {
        this.listeningAddress   = InetAddress.getByName(address);
        this.port               = port;
        this.blocking           = blockingServer;
        this.backlog            = backlog;
    }

    public void connect() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(blocking);
        serverSocketChannel.socket().setReuseAddress(true);
        serverSocketChannel.setOption(
            SO_RCVBUF,
            4096);
        
//        serverSocketChannel.setOption(
//            StandardSocketOptions.IP_TOS,
//            0x08 | 0x10);
        SocketAddress socketAddress = new InetSocketAddress(listeningAddress, port);
        serverSocketChannel.bind(socketAddress, backlog);
    }
    
    public void shutdown() throws IOException {
        serverSocketChannel.close();
    }

    SocketChannel accept() throws IOException {
        return serverSocketChannel.accept();
    }
    
    public SelectionKey register(Selector selector) throws IOException {
        serverSocketChannel.configureBlocking(false);
        return serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

}
