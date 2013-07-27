package com.google.code.jstringserver.server.handlers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import com.google.code.jstringserver.server.bytebuffers.rw.BlockingClientInputHandler;
import com.google.code.jstringserver.server.bytebuffers.store.ByteBufferStore;

/**
 * Not recommended.
 * This is a blocking implementation that does not include timeouts.
 */
public class NaiveClientReader implements ClientReader {
    
    private final ClientDataHandler clientDataHandler;
    
    private final BlockingClientInputHandler clientReader;

    public NaiveClientReader(ByteBufferStore byteBufferStore, ClientDataHandler clientDataHandler) {
        super();
        this.clientDataHandler = clientDataHandler;
        this.clientReader = new BlockingClientInputHandler(byteBufferStore, clientDataHandler);
    }

    @Override
    public void handle(Channel socketChannel) throws IOException {
        clientReader.read((ReadableByteChannel) socketChannel);
        write((WritableByteChannel) socketChannel);
    }

    private void write(WritableByteChannel socketChannel) throws IOException {
        String confirm = clientDataHandler.end(null);
        ByteBuffer byteBuffer = ByteBuffer.wrap(confirm.getBytes());
        byteBuffer.put(confirm.getBytes());
        byteBuffer.flip();
        socketChannel.write(byteBuffer);
    }
    
}
