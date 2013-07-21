package com.google.code.jstringserver.server.nio.select;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public interface AbstractNioReader {

    public abstract int read(SelectionKey key, SocketChannel selectableChannel) throws IOException;

}