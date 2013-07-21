package com.google.code.jstringserver.server.nio.select;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.google.code.jstringserver.server.bytebuffers.store.ByteBufferStore;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;

public class NioReaderLooping extends NioReader {

    public NioReaderLooping(
        ClientDataHandler clientDataHandler,
        ByteBufferStore byteBufferStore) {
        super(clientDataHandler, byteBufferStore);
    }

    @Override
    int read(SelectionKey key, SocketChannel selectableChannel) throws IOException {
        int read = 0;
        do {
            read = super.read(key, selectableChannel);
            if (read == 0) {
                try {
                    Thread.currentThread().sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } while (read != -1 && !finished(key));
        return read;
    }

}
