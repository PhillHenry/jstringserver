package com.google.code.jstringserver.client;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.channels.SocketChannel;


public class BlockingConnector extends Connector {

    public BlockingConnector(String address, int port) {
        super(address, port);
    }

    @Override
    protected void connected(SocketChannel socket) throws IOException {
        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new InterruptedIOException(e.toString());
        }
    }
    
    

}
