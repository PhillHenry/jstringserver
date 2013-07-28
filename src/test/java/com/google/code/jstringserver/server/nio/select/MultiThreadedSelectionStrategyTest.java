package com.google.code.jstringserver.server.nio.select;

import static org.mockito.Mockito.mock;

import java.util.concurrent.ExecutorService;

import org.mockito.Mockito;

public class MultiThreadedSelectionStrategyTest extends AbstractThreadedSelectionStrategyTest {

    private AbstractNioWriter writer          = mock(NioWriter.class);
    private AbstractNioReader reader          = mock(NioReader.class);
    private ExecutorService   executorService = mock(ExecutorService.class);

    protected void postTestChecks() {
        Mockito.verify(executorService).submit(Mockito.any(Runnable.class));
    }

    protected SelectionStrategy strategyToTest() {
        return new MultiThreadedReadingSelectionStrategy(null, serverSelector, writer, reader, executorService);
    }

}
