package com.google.code.jstringserver.server.handlers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.google.code.jstringserver.server.bytebuffers.store.ByteBufferStore;

public class ClientReader {
    
    private final ByteBufferStore byteBufferStore;
    
    private final ClientDataHandler clientDataHandler;

    public ClientReader(ByteBufferStore byteBufferStore, ClientDataHandler clientDataHandler) {
        super();
        this.byteBufferStore = byteBufferStore;
        this.clientDataHandler = clientDataHandler;
    }

    public void handle(SocketChannel socketChannel) throws IOException {
        ByteBuffer byteBuffer = byteBufferStore.getByteBuffer();
        while (socketChannel.read(byteBuffer) != -1) {
            clientDataHandler.handle(byteBuffer);
        }
        clientDataHandler.end();
    }
    
}
