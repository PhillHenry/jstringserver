package com.google.code.jstringserver.server.nio.select;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.google.code.jstringserver.server.bytebuffers.store.ByteBufferStore;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;

public class NioReader implements AbstractNioReader {
    private final ClientDataHandler         clientDataHandler;
    private final ByteBufferStore           byteBufferStore;

    public NioReader(ClientDataHandler clientDataHandler,
                     ByteBufferStore byteBufferStore) {
        super();
        this.clientDataHandler = clientDataHandler;
        this.byteBufferStore = byteBufferStore;
    }

    /* (non-Javadoc)
     * @see com.google.code.jstringserver.server.nio.select.AbstractNioReader#read(java.nio.channels.SelectionKey, java.nio.channels.SocketChannel)
     */
    @Override
    public int read(SelectionKey key, SocketChannel selectableChannel) throws IOException {
        ByteBuffer byteBuffer = byteBufferStore.getByteBuffer();
        byteBuffer.clear();
        int read = selectableChannel.read(byteBuffer);
        byteBuffer.flip();
        clientDataHandler.handle(byteBuffer, key);
        return read;
    }
    
    protected boolean finished(SelectionKey key) {
        return !clientDataHandler.isNotComplete(key);
    }
}
