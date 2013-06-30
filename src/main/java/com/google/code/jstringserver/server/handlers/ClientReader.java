package com.google.code.jstringserver.server.handlers;

import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

public interface ClientReader/*<T extends ReadableByteChannel & WritableByteChannel>*/ {

    public abstract void handle(Channel channel) throws IOException;

}