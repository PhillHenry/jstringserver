package com.google.code.jstringserver.server;

import java.io.IOException;
import java.net.ServerSocket;

public class FreePortFinder {
    
    public int getFreePort() throws IOException {
        ServerSocket serverSocket = new ServerSocket(0);
        serverSocket.setReuseAddress(true);
        try {
            return serverSocket.getLocalPort();
        } finally {
            serverSocket.close();
        }
    }

}
