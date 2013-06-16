package com.google.code.jstringserver.server;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;

public class ReadWriteAcceptor extends Acceptor {

    public ReadWriteAcceptor(Server toTest) {
        super(toTest);
    }

    protected void afterAccept(SocketChannel socketChannel) throws IOException {
        socketChannel.socket().setSoTimeout(100);
        InputStream inputStream = socketChannel.socket().getInputStream();
        ReadableByteChannel channel = Channels.newChannel(inputStream);
        
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        channel.read(byteBuffer);
        byteBuffer.flip();
        
        // TODO do a write
    }
    
}
