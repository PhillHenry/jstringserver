package com.google.code.jstringserver.server.handlers;

import java.nio.ByteBuffer;

public interface ClientDataHandler {

    public int handleRead(ByteBuffer byteBuffer, Object key);

    public String end(Object key);

    public boolean isNotComplete(Object key);

    public abstract int getNumEndCalls();
    
}
