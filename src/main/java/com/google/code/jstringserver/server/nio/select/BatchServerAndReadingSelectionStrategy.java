package com.google.code.jstringserver.server.nio.select;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.code.jstringserver.server.nio.ClientConfigurer;
import com.google.code.jstringserver.server.wait.WaitStrategy;

public class BatchServerAndReadingSelectionStrategy extends AbstractSelectionStrategy {
    
    private final ThreadLocal<Set<SelectionKey>> selectionKeys = new ThreadLocal<Set<SelectionKey>>() {
        @Override
        protected Set<SelectionKey> initialValue() {
            return new HashSet<>();
        }
    };
    
    private final ThreadLocal<Set<SelectionKey>> clientSelectionKeys = new ThreadLocal<Set<SelectionKey>>() {
        @Override
        protected Set<SelectionKey> initialValue() {
            return new HashSet<>();
        }
    };
    
    private final ReaderWriter      readThenWriteJob;
    private final ClientConfigurer  clientConfigurer;

    public BatchServerAndReadingSelectionStrategy(
        WaitStrategy        waitStrategy,
        Selector            selector,
        AbstractNioReader   reader, 
        AbstractNioWriter   writer) {
        super(waitStrategy, selector);
        this.readThenWriteJob = new ReaderWriter(reader, writer);
        this.clientConfigurer = new ClientConfigurer(selector);
    }

    @Override
    protected void handleSelectionKeys() throws IOException {
        handleSelectionKeysWithLock();
        handleClients();
    }

    private void handleClients() throws IOException {
        Set<SelectionKey>       keys        = clientSelectionKeys.get();
        Iterator<SelectionKey>  keyIterator = keys.iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            readThenWriteJob.doWork(key);
            keyIterator.remove();
        }
    }

    private void differentiate(SelectionKey key) throws IOException {
        SelectableChannel channel = key.channel();
        if (channel instanceof ServerSocketChannel) {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) channel;
            SocketChannel       clientChannel       = serverSocketChannel.accept();
            clientConfigurer.register(clientChannel);
        } else {
            key.cancel();
            clientSelectionKeys.get().add(key);
        }
    }
    
    private synchronized void handleSelectionKeysWithLock() throws IOException {
        super.handleSelectionKeys();
        Set<SelectionKey>       keys        = selectionKeys.get();
        Iterator<SelectionKey>  keyIterator = keys.iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            keyIterator.remove();
            differentiate(key);
        }
    }

    @Override
    protected void handle(SelectionKey key) throws IOException {
        selectionKeys.get().add(key);
        if (!(key.channel() instanceof ServerSocketChannel)) {
            key.cancel(); // don't want to process this key again.
        }
    }

}
