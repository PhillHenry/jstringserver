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
        ByteBuffer byteBuffer = byteBufferStore.getByteBuffer(); // java.nio.DirectByteBuffer[pos=0 lim=4096 cap=4096]
        //byteBuffer.flip(); // java.nio.DirectByteBuffer[pos=0 lim=0 cap=4096]
        int read = 0;
        while (clientDataHandler.ready() && (read = socketChannel.read(byteBuffer)) != -1) {
            byteBuffer.flip();
            clientDataHandler.handle(byteBuffer);
            byteBuffer.flip();
        }
        return byteBuffer;
    }
    
}
