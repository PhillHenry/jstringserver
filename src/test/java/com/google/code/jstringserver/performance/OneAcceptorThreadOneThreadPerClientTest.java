package com.google.code.jstringserver.performance;

import com.google.code.jstringserver.server.OneAcceptorThreadOneThreadPerClient;
import com.google.code.jstringserver.server.Server;
import com.google.code.jstringserver.server.ThreadStrategy;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;
import com.google.code.jstringserver.server.handlers.ClientReader;

public class OneAcceptorThreadOneThreadPerClientTest extends AbstractThreadStrategyTest<OneAcceptorThreadOneThreadPerClient> {

    public OneAcceptorThreadOneThreadPerClientTest() {
    }

    @Override
    protected void checkThreadStrategy(OneAcceptorThreadOneThreadPerClient threadStrategy) {
        // TODO Auto-generated method stub
    }

    @Override
    protected OneAcceptorThreadOneThreadPerClient threadingStrategy(Server server, ClientDataHandler clientDataHandler) {
        ClientReader        clientHandler       = createClientReader(clientDataHandler);
        return new OneAcceptorThreadOneThreadPerClient(server, 8, clientHandler);
    }

}
