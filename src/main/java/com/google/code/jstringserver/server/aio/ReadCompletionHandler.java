package com.google.code.jstringserver.server.aio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.google.code.jstringserver.server.handlers.ClientDataHandler;

class ReadCompletionHandler implements CompletionHandler<Integer, Object> {
    
    private final ByteBuffer                byteBuffer;
    private final ClientDataHandler         clientDataHandler;
    private final AsynchronousSocketChannel socketChannel;
    
    public ReadCompletionHandler(
        ByteBuffer                  byteBuffer, 
        ClientDataHandler           clientDataHandler, 
        AsynchronousSocketChannel   socketChannel) {
        super();
        this.byteBuffer         = byteBuffer;
        this.clientDataHandler  = clientDataHandler;
        this.socketChannel      = socketChannel;
    }

    @Override
    public void completed(Integer bytesRead, Object attachment) {
        if (!clientDataHandler.isReadingComplete(attachment)) {
            byteBuffer.flip();
            int read = clientDataHandler.handleRead(byteBuffer, attachment);
            byteBuffer.flip();
            if (!clientDataHandler.isReadingComplete(attachment)) {
                socketChannel.read(byteBuffer, attachment, this);
            }
        }
        
        if (clientDataHandler.isReadingComplete(attachment) 
                && !clientDataHandler.isWritingComplete(attachment)) {
            write(attachment);
        }
    }

    private void write(Object attachment) {
        byte[] toSend = clientDataHandler.end(attachment);
        if (toSend != null) {
            CompletionHandler<Integer, Object> handler = new WriteCompletionHandler(socketChannel, clientDataHandler, byteBuffer);
            Sender.write(attachment, clientDataHandler, byteBuffer, handler, socketChannel);
        } else {
            close();
        }
    }

    private void close() {
        try {
            socketChannel.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void failed(Throwable exc, Object attachment) {
        // TODO Auto-generated method stub
        
    }

}
