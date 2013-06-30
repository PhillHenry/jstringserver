package com.google.code.jstringserver.performance;

import static org.junit.Assert.assertEquals;

import com.google.code.jstringserver.server.OneThreadPerClient;
import com.google.code.jstringserver.server.Server;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;
import com.google.code.jstringserver.server.handlers.ClientReader;

public class OneThreadPerClientServer extends AbstractThreadStrategyTest<OneThreadPerClient> {
    
    protected void checkThreadStrategy(OneThreadPerClient threadStrategy) {
        assertEquals(numExpectedCalls(), threadStrategy.numCallsServiced());
    }

    public static void main(String[] args) throws Exception {
        OneThreadPerClientServer app = new OneThreadPerClientServer();
        app.setUpServer();
        app.shouldProcessAllCalls();
    }
    
    @Override
    protected OneThreadPerClient threadingStrategy(Server server, ClientDataHandler clientDataHandler) {
        ClientReader        clientHandler       = createClientReader(clientDataHandler);
        OneThreadPerClient  oneThreadPerClient  = new OneThreadPerClient(server, 8, clientHandler);
        return oneThreadPerClient;
    }

}
