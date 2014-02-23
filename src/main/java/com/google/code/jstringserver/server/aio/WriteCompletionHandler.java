package com.google.code.jstringserver.server.aio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import com.google.code.jstringserver.server.handlers.ClientDataHandler;

class WriteCompletionHandler implements CompletionHandler<Integer, Object> {

    private final ClientDataHandler         clientDataHandler;
    private final AsynchronousSocketChannel socketChannel;
    private final ByteBuffer                byteBuffer;

    public WriteCompletionHandler(
        AsynchronousSocketChannel   socketChannel,
        ClientDataHandler           clientDataHandler, 
        ByteBuffer                  byteBuffer) {
            this.socketChannel      = socketChannel;
            this.clientDataHandler  = clientDataHandler;
            this.byteBuffer         = byteBuffer;
    }

    @Override
    public void completed(Integer wrote, Object attachment) {
        if (clientDataHandler.isWritingComplete(attachment)) {
            close();
        } else {
            Sender.write(attachment, clientDataHandler, byteBuffer, this, socketChannel);
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
