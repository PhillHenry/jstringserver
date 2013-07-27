package com.google.code.jstringserver.server.nio.select;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.code.jstringserver.server.wait.WaitStrategy;

public class BatchServerAndReadingSelectionStrategy extends AbstractSelectionStrategy {
    
    private final ThreadLocal<Set<SelectionKey>> selectionKeys = new ThreadLocal<Set<SelectionKey>>() {

        @Override
        protected Set<SelectionKey> initialValue() {
            return new HashSet<>();
        }
        
    };
    private final AbstractNioReader reader;

    public BatchServerAndReadingSelectionStrategy(
        WaitStrategy        waitStrategy,
        Selector            selector,
        AbstractNioReader   reader) {
        super(waitStrategy, selector);
        this.reader = reader;
    }

    @Override
    protected void handleSelectionKeys() throws IOException {
        handleSelectionKeysWithLock();
        Set<SelectionKey>       keys        = selectionKeys.get();
        Iterator<SelectionKey>  keyIterator = keys.iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            keyIterator.remove();
            handleClient(key);
        }
    }

    private void handleClient(SelectionKey key) throws IOException {
        SelectableChannel clientChannel = key.channel();
        reader.read(key, (SocketChannel) clientChannel);
    }
    
    private synchronized void handleSelectionKeysWithLock() throws IOException {
        super.handleSelectionKeys();
    }

    @Override
    protected void handle(SelectionKey key) throws IOException {
        selectionKeys.get().add(key);
        key.cancel();
    }

}
