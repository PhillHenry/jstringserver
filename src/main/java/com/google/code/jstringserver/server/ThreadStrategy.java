package com.google.code.jstringserver.server;

public interface ThreadStrategy {

    public void start() throws Exception;

    public void shutdown();
    
}
