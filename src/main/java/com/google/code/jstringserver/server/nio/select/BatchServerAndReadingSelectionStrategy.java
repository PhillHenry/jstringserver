package com.google.code.jstringserver.server.nio.select;

import static java.nio.channels.SelectionKey.OP_CONNECT;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

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
import com.google.code.jstringserver.server.nio.SelectorHolder;
import com.google.code.jstringserver.server.wait.WaitStrategy;
import com.google.code.jstringserver.stats.Stopwatch;

public class BatchServerAndReadingSelectionStrategy implements SelectionStrategy {
    
//    private final ThreadLocal<Set<SelectionKey>> clientSelectionKeys = new ThreadLocal<Set<SelectionKey>>() {
//        @Override
//        protected Set<SelectionKey> initialValue() {
//            return new HashSet<>();
//        }
//    };
    
    private final ThreadLocal<Selector> selectors = new ThreadLocal<Selector>() {
        @Override
        protected Selector initialValue() {
            try {
                return Selector.open();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    };
    
    private final ThreadLocal<ReaderWriter> readWriters = new ThreadLocal<ReaderWriter>() {
        @Override
        protected ReaderWriter initialValue() {
            return readerWriterFactory.createReaderWriter();
        }
    };
    
    
    
    private final ReaderWriterFactory   readerWriterFactory;
    private final ClientConfigurer      clientConfigurer;
    private final WaitStrategy          waitStrategy;
    private final Selector              acceptorSelector;
    private final Stopwatch             stopWatch;

    public BatchServerAndReadingSelectionStrategy(
        WaitStrategy        waitStrategy,
        Selector            selector,
        ReaderWriterFactory readerWriterFactory, 
        Stopwatch           stopWatch) {
        this.waitStrategy        = waitStrategy;
        this.acceptorSelector    = selector;
        this.readerWriterFactory = readerWriterFactory;
        this.stopWatch           = stopWatch;
        this.clientConfigurer    = new ClientConfigurer(new SelectorHolder() {
            @Override
            public Selector getSelector() {
                return selectors.get();
            }
        });
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
        int selected = acceptorSelector.selectNow();
        if (selected > 0) {
            differentiateKeys();
        } 
        return selected;
    }

    private void handleClients() throws IOException {
        Selector                selector    = selectors.get();
        int                     select      = selector.select(10);
        if (select > 0) {
            Set<SelectionKey>       keys        = selector.selectedKeys();
            Iterator<SelectionKey>  keyIterator = keys.iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();
                readWriters.get().doWork(key);
            }
        }
    }

    private void differentiate(SelectionKey key) throws IOException {
        SelectableChannel channel = key.channel();
        if (channel instanceof ServerSocketChannel) {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) channel;
            SocketChannel       clientChannel       = serverSocketChannel.accept();
            clientConfigurer.register(clientChannel);
        } else {
            System.err.println("Shouldn't happen");
//            key.cancel();
//            clientSelectionKeys.get().add(key);
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
        return acceptorSelector.selectedKeys();
    }
    
}
