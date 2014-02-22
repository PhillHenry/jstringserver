package com.google.code.jstringserver.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * @see http://www.javacodegeeks.com/2012/08/io-demystified.html
 */
public class Nio2Server {
    private final InetAddress                       listeningAddress;
    private final int                               port;
    private final int                               backlog;
    private final AsynchronousServerSocketChannel   serverSocketChannel;
    
    public Nio2Server(String address, int port, int backlog) throws IOException {
        this.listeningAddress   = InetAddress.getByName(address);
        this.port               = port;
        this.backlog            = backlog;
        serverSocketChannel = AsynchronousServerSocketChannel.open();
    }
    
    public void connect() throws IOException {
        SocketAddress local = new InetSocketAddress(listeningAddress, port);
        serverSocketChannel.bind(local , backlog);
    }
    
    public void register(CompletionHandler<AsynchronousSocketChannel, Object> handler) throws IOException {
        Object attachment = null;
        serverSocketChannel.accept(attachment , handler);
    }

    public void close() throws IOException {
        serverSocketChannel.close();
    }
}
