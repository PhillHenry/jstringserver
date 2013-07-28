package com.google.code.jstringserver.server.nio.select;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

public class ReadWriteMockFacade {
    
    private final AbstractNioWriter writer = mock(AbstractNioWriter.class);
    private final AbstractNioReader reader = mock(AbstractNioReader.class);

    public void checkReadAndWrite(int expected) throws IOException {
        VerificationMode times = expected == 0 ? Mockito.never() : times(expected);
        verify(writer, times).write(any(SelectionKey.class), any(SocketChannel.class));
        verify(reader, times).read(any(SelectionKey.class), any(SocketChannel.class));
    }

    public AbstractNioWriter getWriter() {
        return writer;
    }

    public AbstractNioReader getReader() {
        return reader;
    }
    
}
