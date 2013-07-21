package com.google.code.jstringserver.server.nio.select;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import com.google.code.jstringserver.server.wait.WaitStrategy;

public class SingleThreadedSelectionStrategy extends AbstractSelectionStrategy {
    private final AbstractNioWriter                 writer;
    private final AbstractNioReader                 reader;
    public SingleThreadedSelectionStrategy(WaitStrategy waitStrategy,
                                           Selector serverSelector, 
                                           AbstractNioWriter writer, 
                                           AbstractNioReader reader) {
        super(waitStrategy, serverSelector);
        this.writer = writer;
        this.reader = reader;
    }

    @Override
    protected void handle(SelectionKey key) throws IOException {
        SocketChannel selectableChannel = (SocketChannel) key.channel();
        if (key.isConnectable()) {
            selectableChannel.finishConnect(); // is this necessary?
        }
        if (key.isReadable()) {
            reader.read(key, selectableChannel);
        }
        if (key.isWritable()) {
            writer.write(key, selectableChannel);
        }
        if (!key.isValid()) {
            selectableChannel.close();
            key.cancel();
        } 
    }
}