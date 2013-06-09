package com.google.code.jstringserver.server;

import java.io.IOException;
import java.nio.channels.SocketChannel;

class Acceptor extends Networker {
    
    private final Server toTest;
    
    public Acceptor(Server toTest) {
        super();
        this.toTest = toTest;
    }

    protected void doCall() throws IOException {
        SocketChannel socketChannel = toTest.accept();
        setError(socketChannel == null);
    }
    
}