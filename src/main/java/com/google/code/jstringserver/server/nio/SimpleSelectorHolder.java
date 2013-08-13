package com.google.code.jstringserver.server.nio;

import java.nio.channels.Selector;

public class SimpleSelectorHolder implements SelectorHolder {
    private final Selector selector;

    public SimpleSelectorHolder(
        Selector selector) {
        super();
        this.selector = selector;
    }

    @Override
    public Selector getSelector() {
        return selector;
    }
    
}