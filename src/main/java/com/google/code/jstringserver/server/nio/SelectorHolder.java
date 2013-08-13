package com.google.code.jstringserver.server.nio;

import java.nio.channels.Selector;

public interface SelectorHolder {
    Selector getSelector();
}