package com.google.code.jstringserver.server.nio.select;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class ChunkedReaderWriter implements ReaderWriter {
    
    private final NioReader         reader;
    private final AbstractNioWriter writer;
    private final Selector          selector;

    public ChunkedReaderWriter(
        NioReader reader,
        AbstractNioWriter writer,
        Selector selector) {
        super();
        this.reader = reader;
        this.writer = writer;
        this.selector = selector;
    }

    @Override
    public void doWork(SelectionKey key) throws IOException {
        SocketChannel selectableChannel = (SocketChannel) key.channel();
        if (reader.finished(key)) {
            writer.write(key, selectableChannel);
            key.cancel();
            selectableChannel.close();
        } else {
            if (key.isReadable()) {
                reader.read(key, selectableChannel);
            }
        }
        
    }

}
