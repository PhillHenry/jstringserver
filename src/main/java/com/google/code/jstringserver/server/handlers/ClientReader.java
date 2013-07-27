package com.google.code.jstringserver.server.handlers;

import java.io.IOException;
import java.nio.channels.Channel;

public interface ClientReader/*<T extends ReadableByteChannel & WritableByteChannel>*/ {

    public abstract void handle(Channel channel) throws IOException;

}