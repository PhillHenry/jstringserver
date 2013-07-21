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
    private final NioWriter writer;
    private final NioReader reader;

    public MultiThreadedSelectionStrategy(
        WaitStrategy    waitStrategy,
        Selector        serverSelector,
        NioWriter       writer, 
        NioReader       reader,
        int             poolSize) {
        super(waitStrategy, serverSelector);
        this.writer = writer;
        this.reader = reader;
        executorService = new ThreadPoolExecutor(
            poolSize, 
            poolSize, 
            Long.MAX_VALUE, 
            TimeUnit.SECONDS, 
            new LinkedBlockingQueue<Runnable>(Integer.MAX_VALUE),
            new NamedThreadFactory(this.getClass().getSimpleName()));
    }

    @Override
    protected void handle(SelectionKey key) throws IOException {
        if (!key.isValid()) {
            executorService.submit(
                new CancelTask(key)
                );
        } else {
//            if (key.isConnectable()) {
//                executorService.submit(
//                    new ConnectableTask(key)
//                    );
//            }
//            if (key.isReadable()) {
//                executorService.submit(
//                    new ReaderTask(key)
//                    );
//            }
            if (key.isReadable() || key.isWritable()) {
                executorService.submit(new ReadWriterTask(key));
            }
            if (key.isWritable()) {
                executorService.submit(
                    new WriterTask(key)
                    );
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
