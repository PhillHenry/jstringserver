package com.google.code.jstringserver.server.exchange;

import java.nio.channels.SocketChannel;

public interface SocketChannelExchanger {
    
    public interface ReadyCallback {
        void ready();
    }
    
    public void setReadyCallback(ReadyCallback readyCallback);

    public void ready(SocketChannel socketChannel);
    
    public SocketChannel consume() throws InterruptedException;
}
