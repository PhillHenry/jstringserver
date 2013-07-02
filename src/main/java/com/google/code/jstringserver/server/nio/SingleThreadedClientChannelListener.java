package com.google.code.jstringserver.server.nio;

import static java.nio.channels.SelectionKey.OP_CONNECT;
import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import com.google.code.jstringserver.server.bytebuffers.store.ByteBufferStore;
import com.google.code.jstringserver.server.handlers.ClientDataHandler;

public class SingleThreadedClientChannelListener implements ClientChannelListener {

    private final ClientDataHandler      clientDataHandler;
    private final ByteBufferStore        byteBufferStore;
    private final SocketChannelExchanger socketChannelExchanger;

    private volatile Selector            selector;
    private volatile boolean             isRunning = true;

    public SingleThreadedClientChannelListener(
                                               ClientDataHandler clientDataHandler,
                                               ByteBufferStore byteBufferStore,
                                               SocketChannelExchanger socketChannelExchanger) {
        super();
        this.clientDataHandler = clientDataHandler;
        this.byteBufferStore = byteBufferStore;
        this.socketChannelExchanger = socketChannelExchanger;
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                SocketChannel socketChannel = socketChannelExchanger.consume();
                if (socketChannel != null) {
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, OP_READ); // | OP_WRITE | OP_CONNECT);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            checkIncoming();
        }
    }

    private void checkIncoming() {
        try {
            int selected = selector.select();// "it can return 0 if the wakeup( ) method of the selector is invoked by another thread."
            if (selected > 0) {
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> selectedKeysIterator = keys.iterator();
                while (selectedKeysIterator.hasNext()) {
                    SelectionKey key = selectedKeysIterator.next();
                    SocketChannel selectableChannel = (SocketChannel) key.channel();
                    if (key.isReadable()) {
                        ByteBuffer byteBuffer = byteBufferStore.getByteBuffer();
                        byteBuffer.clear();
                        selectableChannel.read(byteBuffer);
                        byteBuffer.flip();
                        clientDataHandler.handle(byteBuffer, key);
                    }
                    selectedKeysIterator.remove();
                }
                keys.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        isRunning = false;
    }
    
    public void setSelector(Selector selector) {
        this.selector = selector;
    }

}
