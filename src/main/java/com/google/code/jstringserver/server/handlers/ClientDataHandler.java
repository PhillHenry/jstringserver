package com.google.code.jstringserver.server.handlers;

import java.nio.ByteBuffer;

public interface ClientDataHandler {

    public void handle(ByteBuffer byteBuffer, Object key);

    public String end(Object key);

    public boolean ready();

    public abstract int getNumEndCalls();
    
}
