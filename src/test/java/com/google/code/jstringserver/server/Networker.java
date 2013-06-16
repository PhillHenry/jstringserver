package com.google.code.jstringserver.server;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

abstract class Networker implements Runnable {
    private volatile boolean isError;
    private volatile boolean isFinished;

    @Override
    public void run() {
        try {
            doCall();
        } catch (Exception e) {
            isError = true;
            System.out.println(Thread.currentThread().getName() + " failed " + this);
            e.printStackTrace();
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

    public static void checkFinished(Networker[] networkTasks) {
        for (Networker task : networkTasks) {
            assertTrue(task.isFinished());
        }
    }
    
}