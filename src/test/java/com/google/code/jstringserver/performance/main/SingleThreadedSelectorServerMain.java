package com.google.code.jstringserver.performance.main;

import java.io.IOException;

import com.google.code.jstringserver.server.bytebuffers.store.ByteBufferStore;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;
import com.google.code.jstringserver.server.nio.select.NioReader;
import com.google.code.jstringserver.server.nio.select.NioWriter;
import com.google.code.jstringserver.server.nio.select.SelectionStrategy;
import com.google.code.jstringserver.server.nio.select.SingleThreadedReadingSelectionStrategy;

public class SingleThreadedSelectorServerMain extends AbstractServerMain {

    public static void main(String[] args) throws IOException, InterruptedException {
        SingleThreadedSelectorServerMain app = new SingleThreadedSelectorServerMain();
        app.start(args);
    }
    
    protected SelectionStrategy createSelectionStrategy(ClientDataHandler clientDataHandler, ByteBufferStore byteBufferStore) {
        return new SingleThreadedReadingSelectionStrategy(
            null, 
            null, 
            new NioWriter(clientDataHandler), 
            new NioReader(clientDataHandler, byteBufferStore));
    }

}
