package com.google.code.jstringserver.server;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.code.jstringserver.server.handlers.ClientReader;

public class OneThreadPerClient {

    private final Server          server;
    private final ExecutorService executorService;
    private final ClientReader   clientHandler;
    private final int             numThreads;
    
    private volatile boolean      running = true;

    public OneThreadPerClient(Server server, int numThreads, ClientReader clientHandler) {
        this.clientHandler      = clientHandler;
        this.server             = server;
        this.numThreads         = numThreads;
        this.executorService    = new ThreadPoolExecutor(numThreads, numThreads, 0, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());
    }

    public void start() {
        for (int i = 0 ; i < numThreads ; i++) {
            executorService.execute(new Runnable() {
                
                @Override
                public void run() {
                    while(running) {
                        try {
                            SocketChannel socketChannel = server.accept();
                            clientHandler.handle(socketChannel);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }
    
    public void shutdown() {
        running = false;
        executorService.shutdown();
    }
    
}
