package com.google.code.jstringserver.server.nio.select;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public interface ReaderWriter {

    public abstract void doWork(SelectionKey key) throws IOException;

}