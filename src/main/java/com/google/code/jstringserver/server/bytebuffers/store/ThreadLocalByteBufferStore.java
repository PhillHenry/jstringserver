package com.google.code.jstringserver.server.bytebuffers.store;

import java.nio.ByteBuffer;

import com.google.code.jstringserver.server.bytebuffers.factories.ByteBufferFactory;

public class ThreadLocalByteBufferStore implements ByteBufferStore {
    
    private final ThreadLocal<ByteBuffer> byteBuffers = new ThreadLocal<>();
    
    private final ByteBufferFactory byteBufferFactory;

    public ThreadLocalByteBufferStore(ByteBufferFactory byteBufferFactory) {
        super();
        this.byteBufferFactory = byteBufferFactory;
    }

    @Override
    public ByteBuffer getByteBuffer() {
        ByteBuffer byteBuffer = byteBuffers.get();
        if (byteBuffer == null) {
            byteBuffer = byteBufferFactory.createByteBuffer();
            byteBuffers.set(byteBuffer);
        }
        return byteBuffer;
    }

}
