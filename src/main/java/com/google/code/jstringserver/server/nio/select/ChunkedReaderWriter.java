package com.google.code.jstringserver.server.nio.select;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import com.google.code.jstringserver.stats.Stopwatch;

public class ChunkedReaderWriter implements ReaderWriter {
    
    private final NioReader         reader;
    private final AbstractNioWriter writer;
    private final Stopwatch         stopwatch;

    public ChunkedReaderWriter(
        NioReader reader,
        AbstractNioWriter writer,
        Stopwatch stopwatch) {
        super();
        this.reader = reader;
        this.writer = writer;
        this.stopwatch = stopwatch;
    }

    @Override
    public void doWork(SelectionKey key) throws IOException {
        startTimer();
        try {
            readWrite(key); // if more data comes in since our last call to select, we might not see it
        } finally {
            stopTimer();
        }
    }

    private void readWrite(SelectionKey key) throws IOException {
        SocketChannel selectableChannel = (SocketChannel) key.channel();
        if (reader.finished(key)) {
            int wrote = writer.write(key, selectableChannel);
            if (wrote == -1) {
                close(key, selectableChannel);
            }
        } else {
            if (key.isReadable()) { // if the client has disconnected, this *may* be false
                int read = read(key, selectableChannel);
                if (read == -1) {
                    close(key, selectableChannel);
                }
            }
        }
    }

    protected int read(SelectionKey key, SocketChannel selectableChannel) throws IOException {
        return reader.read(key, selectableChannel);
    }

    protected void close(SelectionKey key, SocketChannel selectableChannel) throws IOException {
        key.cancel();
        selectableChannel.close(); // this can force CLOSE_WAIT -> socket disappears from netstat
    }
    
    private void startTimer() {
        if (stopwatch != null) {
            stopwatch.start();
        }
    }
    
    private void stopTimer() {
        if (stopwatch != null) {
            stopwatch.stop();
        }
    }
}
