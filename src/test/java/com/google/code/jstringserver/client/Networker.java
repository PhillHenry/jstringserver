package com.google.code.jstringserver.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

abstract public class Networker implements Runnable {
    private volatile boolean isError;
    private volatile boolean isFinished;
    private volatile Exception exception;
    private volatile Thread runningThread;

    @Override
    public void run() {
        try {
            runningThread = Thread.currentThread();
            doCall();
        } catch (Exception e) {
            isError = true;
            System.out.println(Thread.currentThread().getName() + " failed " + this);
            e.printStackTrace();
            exception = e;
        }
        isFinished = true;
    }

    abstract protected void doCall() throws Exception;

    public boolean isError() {
        return isError;
    }

    protected void setError(boolean isError) {
        this.isError = isError;
    }

    public boolean isFinished() {
        return isFinished;
    }

    protected void setFinished(boolean isFinished) {
        this.isFinished = isFinished;
    }

    public static void checkNotInError(Networker[] networkTasks) {
        for (Networker task : networkTasks) {
            assertFalse(task.isError());
        }
    }
    
    public static void checkInError(Networker[] networkTasks) {
        for (Networker task : networkTasks) {
            assertTrue(task.isError());
        }
    }

    public static void checkFinished(Networker[] networkTasks) {
        for (Networker task : networkTasks) {
            boolean finished = task.isFinished();
            if (!finished) {
                task.printStackTrace();
            }
            assertTrue(finished);
        }
    }

    public Exception getException() {
        return exception;
    }
    
    public void printStackTrace() {
        System.out.println(Arrays.asList(runningThread.getStackTrace()));
    }
}