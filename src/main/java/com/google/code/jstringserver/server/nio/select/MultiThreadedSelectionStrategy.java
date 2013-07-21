package com.google.code.jstringserver.server.nio.select;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.code.jstringserver.server.threads.NamedThreadFactory;
import com.google.code.jstringserver.server.wait.WaitStrategy;

public class MultiThreadedSelectionStrategy extends AbstractSelectionStrategy {

    private final ExecutorService executorService;
    private final AbstractNioWriter writer;
    private final AbstractNioReader reader;

    public MultiThreadedSelectionStrategy(
        WaitStrategy        waitStrategy,
        Selector            serverSelector,
        AbstractNioWriter   writer, 
        AbstractNioReader   reader,
        ExecutorService     executorService) {
        super(waitStrategy, serverSelector);
        this.writer = writer;
        this.reader = reader;
        this.executorService = executorService;
    }

    @Override
    protected void handle(SelectionKey key) throws IOException {
        if (!key.isValid()) {
            executorService.submit(
                new CancelTask(key)
                );
        } else {
            if (key.isReadable() || key.isWritable()) {
                executorService.submit(new ReadWriterTask(key));
            }
        }
        key.cancel();
    }
    
    abstract class AbstractTask implements Runnable {
        
        private final SelectionKey key;

        public AbstractTask(SelectionKey key) {
            super();
            this.key = key;
        }

        @Override
        public void run() {
            try {
                doWork(key);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        protected SocketChannel getChannel() {
            return (SocketChannel) key.channel();
        }

        protected abstract void doWork(SelectionKey key) throws IOException;
        
    }
    
    class ReadWriterTask extends AbstractTask {

        public ReadWriterTask(
            SelectionKey key) {
            super(key);
        }

        @Override
        protected void doWork(SelectionKey key) throws IOException {
            reader.read(key, getChannel());
            writer.write(key, getChannel());
        }
        
    }
    
    class ReaderTask extends AbstractTask {
        public ReaderTask(
            SelectionKey key) {
            super(key);
        }

        protected void doWork(SelectionKey key) throws IOException {
            reader.read(key, getChannel());
        }
    }
    
    class WriterTask extends AbstractTask {

        public WriterTask(
            SelectionKey key) {
            super(key);
        }

        @Override
        protected void doWork(SelectionKey key) throws IOException {
            writer.write(key, getChannel());
        }
        
    }
    
    class CancelTask extends AbstractTask {

        public CancelTask(
            SelectionKey key) {
            super(key);
        }

        @Override
        protected void doWork(SelectionKey key) throws IOException {
            getChannel().close();
            key.cancel();
        }
        
    }
    
    class ConnectableTask extends AbstractTask {

        public ConnectableTask(
            SelectionKey key) {
            super(key);
        }

        @Override
        protected void doWork(SelectionKey key) throws IOException {
            getChannel().finishConnect(); // is this necessary?
        }
        
    }

}
