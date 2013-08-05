package com.google.code.jstringserver.resources;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.code.jstringserver.client.ConstantWritingConnector;
import com.google.code.jstringserver.performance.main.BatchedServerMain;
import com.google.code.jstringserver.performance.main.ConstantClientsMain;

@Ignore
public class NoWaitTest {

    private BatchedServerMain          serverLifeCycle;
    private ConstantClientsMain        clientsLifeCycle;
    private ConstantWritingConnector[] clients;

    @Before
    public void startServerAndClients() throws UnknownHostException, IOException, Exception {
        serverLifeCycle = new BatchedServerMain();
        serverLifeCycle.startServer(new String[] {});

        clientsLifeCycle = new ConstantClientsMain();
        clients = clientsLifeCycle.run(new String[] { 
            "10",
            "localhost", 
            });
    }

    @After
    public void shutdown() throws IOException {
        System.out.println("Shutting down server and clients");
        serverLifeCycle.stop();
        for (ConstantWritingConnector client : clients) {
            client.stop();
        }
    }

    @Test
    public void checkNoResourceExaustion() throws InterruptedException {
        long expectedDurationMs = 60000;
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() < (start + expectedDurationMs)) {
            long maxTime = clientsLifeCycle.getConnectStopWatch().getMaxTime();
            if (maxTime > 5000) {
                fail("Max time = " + maxTime);
            }
            Thread.sleep(1000);
            System.out.print(".");
        }
    }

}
