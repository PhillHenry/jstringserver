package com.google.code.jstringserver.server.nio;

import static java.net.StandardSocketOptions.SO_LINGER;
import static java.net.StandardSocketOptions.SO_RCVBUF;
import static java.nio.channels.SelectionKey.OP_CONNECT;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

import java.io.IOException;
import java.net.Socket;
import java.net.StandardSocketOptions;
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
//            socketChannel.setOption(
//                SO_LINGER,
//                1);
            socketChannel.setOption(
                SO_RCVBUF,
                4096);
            
            socketChannel.setOption(
                StandardSocketOptions.IP_TOS,
                0x08 | 0x10);
            
            Socket socket = socketChannel.socket();
            socket.setTcpNoDelay(false);
            
            // doesn't seem to work anyway if timeout > 0
            socket.setSoLinger(true, 0); // <-- this is important! Stevens warns against it but line below seems insufficient...
            
            socket.setReuseAddress(true); // <-- but this is better, see Unix Network Programming, Stevens et al. 
//            if (socket.isConnected()) { // true irrespective of whether the client has disconnected
//            }
            socketChannel.configureBlocking(false);
            Selector selector = selectorHolder.getSelector();
            socketChannel.register(selector, OP_READ | OP_WRITE); // can block if other threads are selecting. see http://stackoverflow.com/questions/1057224/thread-is-stuck-while-registering-channel-with-selector-in-java-nio-server
        }
    }

}
