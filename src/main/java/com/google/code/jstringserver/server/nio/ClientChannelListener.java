package com.google.code.jstringserver.server.nio;

import java.nio.channels.Selector;

public interface ClientChannelListener extends Runnable {
    public void shutdown();
    public void setSelector(Selector selector);
}
