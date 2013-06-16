package com.google.code.jstringserver.server.handlers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.google.code.jstringserver.server.bytebuffers.store.ByteBufferStore;

/**
 * Not recommended.
 * This is a blocking implementation that does not include timeouts.
 */
public class NaiveClientReader implements ClientReader {
    
    private final ByteBufferStore byteBufferStore;
    
    private final ClientDataHandler clientDataHandler;

    public NaiveClientReader(ByteBufferStore byteBufferStore, ClientDataHandler clientDataHandler) {
        super();
        this.byteBufferStore = byteBufferStore;
        this.clientDataHandler = clientDataHandler;
    }

    @Override
    public void handle(SocketChannel socketChannel) throws IOException {
        read(socketChannel);
        write(socketChannel);
    }

    private void write(SocketChannel socketChannel) throws IOException {
        String confirm = clientDataHandler.end();
        ByteBuffer byteBuffer = ByteBuffer.wrap(confirm.getBytes());
        byteBuffer.put(confirm.getBytes());
        byteBuffer.flip();
        socketChannel.write(byteBuffer);
    }

    private ByteBuffer read(SocketChannel socketChannel) throws IOException {
        ByteBuffer byteBuffer = byteBufferStore.getByteBuffer();
        int read = 0;
        while (clientDataHandler.ready() && (read = socketChannel.read(byteBuffer)) != -1) {
            byteBuffer.flip();
            clientDataHandler.handle(byteBuffer);
            byteBuffer.flip();
        }
        return byteBuffer;
    }
    
}
