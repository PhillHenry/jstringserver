package com.google.code.jstringserver.server.nio;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

import com.google.code.jstringserver.server.wait.WaitStrategy;

public abstract class AbstractSelectionStrategy {

    private final WaitStrategy           waitStrategy;
    private final Selector               serverSelector;
    
    public AbstractSelectionStrategy(WaitStrategy waitStrategy,
                            Selector serverSelector) {
        super();
        this.waitStrategy = waitStrategy;
        this.serverSelector = serverSelector;
    }
    
    public void select() throws IOException {
        // see http://people.freebsd.org/~jlemon/papers/kqueue.pdf
        // "Kevent - structue which delivers event to user. poll/select does not scale well"
        // - http://people.freebsd.org/~jlemon/kqueue_slides/sld006.htm
        int selected = serverSelector.select(); // sun.nio.ch.KQueueSelectorImpl
        if (selected > 0) {
            handleSelectionKeys();
        } else {
            if (waitStrategy != null) {
                waitStrategy.pause();
            }
        }
    }
    
    private void handleSelectionKeys() throws IOException {
        Set<SelectionKey> keys = serverSelector.selectedKeys();
        Iterator<SelectionKey> keyIter = keys.iterator();
        while (keyIter.hasNext()) {
            SelectionKey key = keyIter.next();
            handle(key);
            keyIter.remove();
        }
    }

    protected abstract void handle(SelectionKey key) throws IOException;
}
