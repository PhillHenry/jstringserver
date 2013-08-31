package com.google.code.jstringserver.server.nio.select;

import static com.google.code.jstringserver.client.Connector.createConnectors;
import static com.google.code.jstringserver.server.nio.select.ServerTestSetup.HOST;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.code.jstringserver.client.Connector;
import com.google.code.jstringserver.server.AbstractMultiThreadedTest;
import com.google.code.jstringserver.server.FreePortFinder;
import com.google.code.jstringserver.server.Server;
import com.google.code.jstringserver.server.wait.WaitStrategy;

public class SimpleSelectionStrategyTest extends AbstractMultiThreadedTest {

    private final class ShuntSelectionStrategyExtension extends AbstractSelectionStrategy {
        
        final Set<SelectionKey> selectionKeys = new HashSet<>();
        
        private ShuntSelectionStrategyExtension(
            WaitStrategy waitStrategy,
            Selector selector) {
            super(waitStrategy, selector);
        }

        @Override
        protected void handle(SelectionKey key) throws IOException {
            selectionKeys.add(key);
        }

        public Set<SelectionKey> getSelectionKeys() {
            return selectionKeys;
        }
        
        public void clear() {
            selectionKeys.clear();
        }
    }
    private ShuntSelectionStrategyExtension toTest;
    private ServerTestSetup serverTestSetup;

    @Before
    public void setUp() throws IOException {
        serverTestSetup = new ServerTestSetup();
        toTest = new ShuntSelectionStrategyExtension(null, serverTestSetup.getSelector());
    }

    @After
    public void shutdown() throws IOException {
        serverTestSetup.shutdown();
    }
    
    @Test
    public void levelTriggered() throws IOException {
        Connector[] connectors = createConnectors(1, HOST, serverTestSetup.getPort());
        start(connectors, "first connector");
        Set<SelectionKey> firstSelect = doSelect();
        toTest.clear();
        Set<SelectionKey> secondSelect = doSelect();
        assertEquals(firstSelect, secondSelect);
    }

    private Set<SelectionKey> doSelect() throws IOException {
        toTest.select();
        Set<SelectionKey> keys = toTest.getSelectionKeys();
        assertEquals(keys.size(), 1);
        return keys;
    }
}
