package com.google.code.jstringserver.server.bytebuffers.rw;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import com.google.code.jstringserver.server.bytebuffers.store.ByteBufferStore;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;

public class BlockingClientInputHandler {
    
    private final ByteBufferStore byteBufferStore;
    
    private final ClientDataHandler clientDataHandler;
    
    public BlockingClientInputHandler(ByteBufferStore byteBufferStore, ClientDataHandler clientDataHandler) {
        super();
        this.byteBufferStore = byteBufferStore;
        this.clientDataHandler = clientDataHandler;
    }

    public ByteBuffer read(ReadableByteChannel socketChannel) throws IOException {
        ByteBuffer byteBuffer = byteBufferStore.getByteBuffer();
        int read = 0;
        while (clientDataHandler.isNotComplete(null) && (read = socketChannel.read(byteBuffer)) != -1) {
            byteBuffer.flip();
            clientDataHandler.handleRead(byteBuffer, null);
            byteBuffer.flip();
        }
        return byteBuffer;
    }

}
