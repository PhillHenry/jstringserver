package com.google.code.jstringserver.server.nio.select;

import java.io.IOException;

public interface SelectionStrategy {

    public void select() throws IOException;

}