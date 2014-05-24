package com.google.code.jstringserver.server.aio;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import com.google.code.jstringserver.server.bytebuffers.factories.ByteBufferFactory;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;

public class AcceptCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {
    
    private final ByteBufferFactory                 byteBufferFactory;
    private final ClientDataHandler                 clientDataHandler;
    private final AsynchronousServerSocketChannel   serverSocketChannel;
    
    public AcceptCompletionHandler(
        ByteBufferFactory               byteBufferFactory,
        ClientDataHandler               clientDataHandler,
        AsynchronousServerSocketChannel serverSocketChannel) {
        super();
        this.byteBufferFactory      = byteBufferFactory;
        this.clientDataHandler      = clientDataHandler;
        this.serverSocketChannel    = serverSocketChannel;
    }

    @Override
    public void completed(AsynchronousSocketChannel socketChannel, Object attachment) {
        serverSocketChannel.accept(null, this);
        ByteBuffer                          byteBuffer  = byteBufferFactory.createByteBuffer();
        CompletionHandler<Integer, Object>  handler     = new ReadCompletionHandler(byteBuffer, clientDataHandler, socketChannel);
        socketChannel.read(byteBuffer, newKey(), handler);
    }

    private Object newKey() {
        return clientDataHandler.getKey();
    }

    @Override
    public void failed(Throwable exc, Object attachment) {
        // TODO Auto-generated method stub
        exc.printStackTrace(); // TODO
    }

}
