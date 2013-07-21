package com.google.code.jstringserver.server.nio.select;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.google.code.jstringserver.server.handlers.ClientDataHandler;

public class NioWriter implements AbstractNioWriter {
    
    private final ClientDataHandler         clientDataHandler;

    public NioWriter(ClientDataHandler clientDataHandler) {
        super();
        this.clientDataHandler = clientDataHandler;
    }

    @Override
    public void write(SelectionKey key, SocketChannel selectableChannel) throws IOException {
        String messageBack = clientDataHandler.end(key);
        if (messageBack != null) {
            ByteBuffer buffer = ByteBuffer.wrap(messageBack.getBytes()); // TODO
                                                                         // optimize
            selectableChannel.write(buffer);
            key.cancel();
        }
    }
}
