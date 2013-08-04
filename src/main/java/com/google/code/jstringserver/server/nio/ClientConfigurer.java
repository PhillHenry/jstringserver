package com.google.code.jstringserver.server.nio;

import static java.nio.channels.SelectionKey.OP_CONNECT;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class ClientConfigurer {

    private final Selector selector;

    public ClientConfigurer(
        Selector selector) {
            this.selector = selector;
    }

    public void register(SocketChannel socketChannel) throws IOException, ClosedChannelException {
        if (socketChannel != null) {
            Socket socket = socketChannel.socket();
//            if (socket.isConnected())
            socket.setSoLinger(true, 0);
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, OP_READ | OP_CONNECT | OP_WRITE); // can block if other threads are selecting. see http://stackoverflow.com/questions/1057224/thread-is-stuck-while-registering-channel-with-selector-in-java-nio-server
        }
    }

}
