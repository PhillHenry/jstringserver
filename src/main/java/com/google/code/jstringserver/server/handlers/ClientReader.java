package com.google.code.jstringserver.server.handlers;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public interface ClientReader {

    public abstract void handle(SocketChannel socketChannel) throws IOException;

}