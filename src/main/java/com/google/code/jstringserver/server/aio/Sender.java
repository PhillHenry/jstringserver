package com.google.code.jstringserver.server.aio;

import static java.lang.Math.min;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import com.google.code.jstringserver.server.handlers.ClientDataHandler;

public class Sender {
    static byte[] write(
        Object                              attachment, 
        ClientDataHandler                   clientDataHandler, 
        ByteBuffer                          byteBuffer, 
        CompletionHandler<Integer, Object>  writeCompletionHandler,
        AsynchronousSocketChannel           socketChannel) {
        
        byte[] toSend = clientDataHandler.end(attachment);
        if (toSend != null) {
            write(attachment, byteBuffer, writeCompletionHandler,
                    socketChannel, toSend);
        } 
        return toSend;
    }

    private static void write(Object attachment, 
            ByteBuffer                          byteBuffer,
            CompletionHandler<Integer, Object>  writeCompletionHandler,
            AsynchronousSocketChannel           socketChannel, 
            byte[]                              toSend) {
        int toWrite = min(toSend.length, byteBuffer.capacity());
        byteBuffer.clear();
        byteBuffer.put(toSend, 0, toWrite);
        byteBuffer.flip();
        socketChannel.write(byteBuffer, attachment, writeCompletionHandler);
    }
}
