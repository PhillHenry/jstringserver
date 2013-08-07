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
import com.google.code.jstringserver.stats.Stopwatch;

public class BatchServerAndReadingSelectionStrategy implements SelectionStrategy {
    
    private final ThreadLocal<Set<SelectionKey>> clientSelectionKeys = new ThreadLocal<Set<SelectionKey>>() {
        @Override
        protected Set<SelectionKey> initialValue() {
            return new HashSet<>();
        }
    };
    
    private final ReaderThenWriter      readerWriter;
    private final ClientConfigurer  clientConfigurer;
    private final WaitStrategy      waitStrategy;
    private final Selector          selector;
    private final Stopwatch         stopWatch;

    public BatchServerAndReadingSelectionStrategy(
        WaitStrategy        waitStrategy,
        Selector            selector,
        AbstractNioReader   reader, 
        AbstractNioWriter   writer, 
        Stopwatch           stopWatch) {
        this.waitStrategy       = waitStrategy;
        this.selector           = selector;
        this.stopWatch          = stopWatch;
        this.readerWriter       = new ReaderThenWriter(reader, writer);
        this.clientConfigurer   = new ClientConfigurer(selector);
    }
    
    @Override
    public void select() throws IOException {
        startTimer();
        try {
            int selected = selectWithLock(); 
            if (selected == 0 && waitStrategy != null) {
                waitStrategy.pause();
            }
            handleClients();
        } finally {
            stopTimer();
        }
    }

    private void stopTimer() {
        if (stopWatch != null) {
            stopWatch.stop();
        }
    }

    private void startTimer() {
        if (stopWatch != null) {
            stopWatch.start();
        }
    }

    private synchronized int selectWithLock() throws IOException {
        int selected = selector.selectNow();
        if (selected > 0) {
            differentiateKeys();
        } 
        return selected;
    }

    private void handleClients() throws IOException {
        Set<SelectionKey>       keys        = clientSelectionKeys.get();
        Iterator<SelectionKey>  keyIterator = keys.iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            keyIterator.remove();
            readerWriter.doWork(key);
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
    
    private Set<SelectionKey> differentiateKeys() throws IOException {
        Set<SelectionKey>       keys        = selected();
        Iterator<SelectionKey>  keyIterator = keys.iterator();
        while (keyIterator.hasNext()) {
            SelectionKey key = keyIterator.next();
            keyIterator.remove();
            differentiate(key);
        }
        return keys;
    }

    Set<SelectionKey> selected() {
        return selector.selectedKeys();
    }
    
}
