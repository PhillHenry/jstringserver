package com.google.code.jstringserver.server.handlers;

import java.nio.ByteBuffer;

public interface ClientDataHandler {
    
    public Object getKey();

    public int handleRead(ByteBuffer byteBuffer, Object key);
    
    public void handleWrite(int wrote, Object key);

    public byte[] end(Object key);

    public boolean isReadingComplete(Object key);
    
    public boolean isWritingComplete(Object key);
    
    public boolean isTimedOut(Object key);

    public abstract int getNumEndCalls();
    
}
