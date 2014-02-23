package com.google.code.jstringserver.server.aio;

import static java.lang.Math.max;

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
            byteBuffer.rewind();
            byteBuffer.put(toSend, 0, max(toSend.length, byteBuffer.capacity()));
            socketChannel.write(byteBuffer, attachment, writeCompletionHandler);
        } 
        return toSend;
    }
}
