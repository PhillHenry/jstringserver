package com.google.code.jstringserver.server.nio;

import java.nio.channels.SocketChannel;

public interface SocketChannelExchanger {
    
    public interface ReadyCallback {
        void ready();
    }
    
    public void setReadyCallback(ReadyCallback readyCallback);

    public void ready(SocketChannel socketChannel);
    
    public SocketChannel consume() throws InterruptedException;
}
