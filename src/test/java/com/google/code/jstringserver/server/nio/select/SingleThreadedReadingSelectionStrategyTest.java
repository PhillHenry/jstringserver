package com.google.code.jstringserver.server.nio.select;

import static com.google.code.jstringserver.server.nio.select.ServerTestSetup.HOST;
import static java.net.StandardSocketOptions.SO_LINGER;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

import com.google.code.jstringserver.client.Connector;
import com.google.code.jstringserver.server.AbstractMultiThreadedTest;

public class SingleThreadedReadingSelectionStrategyTest extends AbstractMultiThreadedTest {

    private SingleThreadedReadingSelectionStrategy toTest;

    private ServerTestSetup                        serverTestSetup;
    private ReadWriteMockFacade                    mocks;
    private Selector                               clientSelector;
    private ClientTestSetup                        clientTestSetup;
    private CountDownLatch                         postClose;

    @Before
    public void setUp() throws IOException {
        serverTestSetup = new ServerTestSetup();
        clientSelector = Selector.open();
        mocks = new ReadWriteMockFacade();
        toTest = new SingleThreadedReadingSelectionStrategy(null, clientSelector, mocks.getWriter(), mocks.getReader());
    }

    @After
    public void shutdown() throws IOException {
        serverTestSetup.shutdown();
    }

    @Test
    @Ignore
    // don't know how we can read/write when the client has disconnected...
    public void disconnectsImmediately() throws IOException, InterruptedException {
        clientsConnectThenDisconnect();
        ClientTestSetup.await(postClose);
        toTest.select();
        mocks.checkReadAndWrite(0);
    }

    @Test
    public void selectLifecycle() throws IOException, InterruptedException {
        clientsConnectAndReadyForReadWrite();

        clientTestSetup.awaitPostRead();
        clientTestSetup.awaitPreWrite();
        toTest.select();
        mocks.checkReadAndWrite(1);
    }

    private void clientsConnectThenDisconnect() throws IOException {
        Connector[] connectors = new Connector[] { new Connector(HOST, serverTestSetup.getPort()) {

            @Override
            protected void configure(InetSocketAddress inetSocketAddress, SocketChannel socketChannel) throws IOException {
                super.configure(inetSocketAddress, socketChannel);
                socketChannel.setOption(SO_LINGER, 0);
            }

            @Override
            protected void close(SocketChannel socketChannel) throws IOException {
                socketChannel.configureBlocking(true);
                super.close(socketChannel);
                socketChannel.socket().close();
                postClose.countDown();
            }

        } };
        postClose = new CountDownLatch(connectors.length);
        start(connectors, "connectors");
        serverTestSetup.accept(clientSelector);
    }

    private void clientsConnectAndReadyForReadWrite() throws IOException {
        clientTestSetup = new ClientTestSetup(serverTestSetup, 1);
        start(clientTestSetup.createLatchedClients(), "rw");
        serverTestSetup.accept(clientSelector);
    }

}
