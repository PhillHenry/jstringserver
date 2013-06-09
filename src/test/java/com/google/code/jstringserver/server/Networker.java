package com.google.code.jstringserver.server;

abstract class Networker implements Runnable {
    private volatile boolean isError;
    private volatile boolean isFinished;

    @Override
    public void run() {
        try {
            doCall();
        } catch (Exception e) {
            e.printStackTrace();
            isError = true;
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
    
}