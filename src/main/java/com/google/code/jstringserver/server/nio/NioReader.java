package com.google.code.jstringserver.server.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.google.code.jstringserver.server.bytebuffers.store.ByteBufferStore;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;

public class NioReader {
    private final ClientDataHandler         clientDataHandler;
    private final ByteBufferStore           byteBufferStore;

    public NioReader(ClientDataHandler clientDataHandler,
                     ByteBufferStore byteBufferStore) {
        super();
        this.clientDataHandler = clientDataHandler;
        this.byteBufferStore = byteBufferStore;
    }

    void read(SelectionKey key, SocketChannel selectableChannel) throws IOException {
        ByteBuffer byteBuffer = byteBufferStore.getByteBuffer();
        byteBuffer.clear();
        selectableChannel.read(byteBuffer);
        byteBuffer.flip();
        clientDataHandler.handle(byteBuffer, key);
    }
}
