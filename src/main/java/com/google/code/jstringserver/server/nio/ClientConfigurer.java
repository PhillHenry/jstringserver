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
    
    private final SelectorHolder selectorHolder;

    public ClientConfigurer(
        SelectorHolder selectorHolder) {
            this.selectorHolder = selectorHolder;
    }

    public void register(SocketChannel socketChannel) throws IOException, ClosedChannelException {
        if (socketChannel != null) {
            Socket socket = socketChannel.socket();
            socket.setTcpNoDelay(true);
            if (socket.isConnected()) { // true irrespective of whether the client has disconnected
//                socket.setSoLinger(true, 0); // <-- this is important! Stevens warns against it but line below seems insufficient...
                socket.setReuseAddress(true); // <-- but this is better, see Unix Network Programming, Stevens et al. 
            }
            socketChannel.configureBlocking(false);
            Selector selector = selectorHolder.getSelector();
            socketChannel.register(selector, OP_READ | OP_CONNECT | OP_WRITE); // can block if other threads are selecting. see http://stackoverflow.com/questions/1057224/thread-is-stuck-while-registering-channel-with-selector-in-java-nio-server
        }
    }

}
