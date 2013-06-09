package com.google.code.jstringserver.server.bytebuffers.factories;

import java.nio.ByteBuffer;

public class DirectByteBufferFactory implements ByteBufferFactory {

    private final int capacity;

    public DirectByteBufferFactory(int capacity) {
        super();
        this.capacity = capacity;
    }

    @Override
    public ByteBuffer createByteBuffer() {
        return ByteBuffer.allocateDirect(capacity);
    }

}
