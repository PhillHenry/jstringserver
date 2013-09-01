package com.google.code.jstringserver.server.nio.select;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.google.code.jstringserver.server.handlers.ClientDataHandler;
import com.google.code.jstringserver.stats.Stopwatch;

public class NioWriter implements AbstractNioWriter {

    private final ClientDataHandler clientDataHandler;
    private final Stopwatch         stopwatch;

    public NioWriter(
        ClientDataHandler clientDataHandler,
        Stopwatch stopwatch) {
        super();
        this.clientDataHandler = clientDataHandler;
        this.stopwatch = stopwatch;
    }

    @Override
    public void write(SelectionKey key, SocketChannel selectableChannel) throws IOException {
        startTimer();
        try {
            doWrite(key, selectableChannel);
        } finally {
            stopTimer();
        }
    }

    private void stopTimer() {
        if (stopwatch != null) {
            stopwatch.stop();
        }
    }

    private void doWrite(SelectionKey key, SocketChannel selectableChannel) throws IOException {
        String messageBack = clientDataHandler.end(key);
        if (messageBack != null) {
            ByteBuffer buffer = ByteBuffer.wrap(messageBack.getBytes()); // TODO
                                                                         // optimize
            selectableChannel.write(buffer); // if the client side has closed, this can force its socket to go from CLOSED_WAIT -> finished
            key.cancel();
        }
    }

    private void startTimer() {
        if (stopwatch != null) {
            stopwatch.start();
        }
    }
}
