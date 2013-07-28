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

public class BatchServerAndReadingSelectionStrategy implements SelectionStrategy {
    
    private final ThreadLocal<Set<SelectionKey>> clientSelectionKeys = new ThreadLocal<Set<SelectionKey>>() {
        @Override
        protected Set<SelectionKey> initialValue() {
            return new HashSet<>();
        }
    };
    
    private final ReaderWriter      readThenWriteJob;
    private final ClientConfigurer  clientConfigurer;
    private final WaitStrategy waitStrategy;
    private final Selector selector;

    public BatchServerAndReadingSelectionStrategy(
        WaitStrategy        waitStrategy,
        Selector            selector,
        AbstractNioReader   reader, 
        AbstractNioWriter   writer) {
        this.waitStrategy       = waitStrategy;
        this.selector           = selector;
        this.readThenWriteJob   = new ReaderWriter(reader, writer);
        this.clientConfigurer   = new ClientConfigurer(selector);
    }
    
    @Override
    public synchronized void select() throws IOException {
        int selected = selector.select(); 
        if (selected > 0) {
            handleSelectionKeys();
        } else {
            if (waitStrategy != null) {
                waitStrategy.pause();
            }
        }
    }

    protected void handleSelectionKeys() throws IOException {
        handleSelectionKeysWithLock();
        handleClients();
    }

    private void handleClients() throws IOException {
        Set<SelectionKey>       keys        = clientSelectionKeys.get();
        Iterator<SelectionKey>  keyIterator = keys.iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            SocketChannel clientChannel = (SocketChannel) key.channel();
            clientChannel.finishConnect();
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
        Set<SelectionKey>       keys        = selector.selectedKeys();
        Iterator<SelectionKey>  keyIterator = keys.iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            keyIterator.remove();
            differentiate(key);
        }
    }
    
}
