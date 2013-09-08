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
    public int write(SelectionKey key, SocketChannel selectableChannel) throws IOException {
        startTimer();
        try {
            return doWrite(key, selectableChannel);
        } finally {
            stopTimer();
        }
    }

    private void stopTimer() {
        if (stopwatch != null) {
            stopwatch.stop();
        }
    }

    private int doWrite(SelectionKey key, SocketChannel selectableChannel) throws IOException {
        int wrote = 0;
        byte[] bytes = clientDataHandler.end(key);
        if (bytes != null) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes); // TODO - optimize
            wrote = selectableChannel.write(buffer); // if the client side has closed, this can force its socket to go from CLOSED_WAIT -> finished
            afterWrite(key, wrote);
        }
        return wrote;
    }

    private void afterWrite(SelectionKey key, int wrote) throws IOException {
        if (wrote > 0) {
            clientDataHandler.handleWrite(wrote, key);
        }
        if (wrote == -1 || clientDataHandler.isWritingComplete(key)) {
            key.cancel();
            ((SocketChannel)key.channel()).close();
            key.channel().close();
        }
    }

    private void startTimer() {
        if (stopwatch != null) {
            stopwatch.start();
        }
    }
}
