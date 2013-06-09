package com.google.code.jstringserver.server.handlers;

import java.nio.ByteBuffer;

public interface ClientDataHandler {

    public void handle(ByteBuffer byteBuffer);

    public void end();
    
}
