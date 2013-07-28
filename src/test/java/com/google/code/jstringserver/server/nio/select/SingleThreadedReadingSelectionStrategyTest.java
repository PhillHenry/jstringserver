package com.google.code.jstringserver.server.nio.select;

import static com.google.code.jstringserver.server.nio.select.ServerTestSetup.HOST;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.code.jstringserver.client.Connector;
import com.google.code.jstringserver.server.AbstractMultiThreadedTest;
import com.google.code.jstringserver.server.nio.ClientConfigurer;

public class SingleThreadedReadingSelectionStrategyTest extends AbstractMultiThreadedTest {
    
    private SingleThreadedReadingSelectionStrategy toTest;
    
    private ServerTestSetup serverTestSetup;
    private AbstractNioWriter writer = Mockito.mock(AbstractNioWriter.class);
    private AbstractNioReader reader = Mockito.mock(AbstractNioReader.class);

    private Selector clientSelector;

    @Before
    public void setUp() throws IOException {
        serverTestSetup = new ServerTestSetup();
        clientSelector = Selector.open();
        toTest = new SingleThreadedReadingSelectionStrategy(null, clientSelector, writer, reader);
    }
    
    @After
    public void shutdown() throws IOException {
        serverTestSetup.shutdown();
    }
    
    @Test
    public void select() throws IOException {
        Connector[] connectors = Connector.createConnectors(1, HOST, serverTestSetup.getPort());
        start(connectors, "connectors");
        serverTestSetup.accept(clientSelector);
        
        toTest.select();
        verify(writer).write(Mockito.any(SelectionKey.class), Mockito.any(SocketChannel.class));
    }

}
