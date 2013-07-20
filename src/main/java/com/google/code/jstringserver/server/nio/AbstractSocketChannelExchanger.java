package com.google.code.jstringserver.server.nio;

import java.nio.channels.SocketChannel;

public abstract class AbstractSocketChannelExchanger implements SocketChannelExchanger {

    private volatile ReadyCallback readyCallback;
    
    @Override
    public void ready(SocketChannel socketChannel) {
        add(socketChannel);
        if (readyCallback != null) {
            readyCallback.ready();
        }
    }

    @Override
    public void setReadyCallback(ReadyCallback readyCallback) {
        this.readyCallback = readyCallback;
    }

    protected abstract void add(SocketChannel socketChannel);
}
