package com.google.code.jstringserver.server.nio.select;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ReaderThenWriter {
    private final AbstractNioWriter writer;
    private final AbstractNioReader reader;

    public ReaderThenWriter(
        AbstractNioReader reader,
        AbstractNioWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    public void doWork(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            reader.read(key, channel);
            writer.write(key, channel);
        } finally {
            channel.socket().close();
            channel.close();
        }
    }

}
