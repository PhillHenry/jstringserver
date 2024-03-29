package com.google.code.jstringserver.server;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import com.google.code.jstringserver.server.handlers.ClientReader;

public class OneAcceptorThreadOneThreadPerClient implements ThreadStrategy {
    
    private final Server          server;
    private final ExecutorService executorService;
    private final ClientReader    clientHandler;
    
    private volatile boolean      running = true;

    public OneAcceptorThreadOneThreadPerClient(Server server, int numThreads, ClientReader clientHandler) {
        this.clientHandler      = clientHandler;
        this.server             = server;
        this.executorService    = new ThreadPoolFactory(numThreads).createThreadPoolExecutor();
    }

    @Override
    public void start() {
        Thread thread = new Thread(new Runnable() {
            
            @Override
            public void run() {
                while (running) {
                    try {
                        final SocketChannel socketChannel = server.accept();
                        executorService.submit(new Callable<Void>() {

                            @Override
                            public Void call() throws Exception {
                                try {
                                    clientHandler.handle(socketChannel);
                                    socketChannel.close();
                                } catch (Exception x) {
                                    x.printStackTrace();
                                    throw x;
                                }
                                return null;
                            }
                        });
                    } catch (IOException e) {
                        if (running)
                            e.printStackTrace();
                    }
                }
            }
        }, "acceptor thread");
        thread.start();
    }

    @Override
    public void shutdown() {
        running = false;
        executorService.shutdown();
    }

}
