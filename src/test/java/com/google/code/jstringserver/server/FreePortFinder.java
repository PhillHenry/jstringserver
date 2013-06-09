package com.google.code.jstringserver.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class FreePortFinder {
    
    public int getFreePort() throws IOException {
//        InetSocketAddress inetSocketAddress = new InetSocketAddress(0);
//        Socket socket = new Socket();
        ServerSocket serverSocket = new ServerSocket(0);
        try {
//            serverSocket.bind(inetSocketAddress);
//            socket.bind(inetSocketAddress);
            return serverSocket.getLocalPort();
        } finally {
//            socket.close();
            serverSocket.close();
        }
    }

}
