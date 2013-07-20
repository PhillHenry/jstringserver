package com.google.code.jstringserver.server;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import com.google.code.jstringserver.client.Networker;

class Acceptor extends Networker {
    
    protected final Server toTest;
    
    public Acceptor(Server toTest) {
        super();
        this.toTest = toTest;
    }

    protected void doCall() throws IOException {
        SocketChannel socketChannel = toTest.accept();
        setError(socketChannel == null);
        afterAccept(socketChannel);
    }

    protected void afterAccept(SocketChannel socketChannel) throws IOException {
        // NoOp
    }

    public static Acceptor[] createAcceptors(int numConnectors, Server toTest) {
        Acceptor[] connectors = new Acceptor[numConnectors];
        for (int i = 0 ; i < numConnectors ; i++) {
            connectors[i] = new Acceptor(toTest);
        }
        return connectors;
    }
    
}