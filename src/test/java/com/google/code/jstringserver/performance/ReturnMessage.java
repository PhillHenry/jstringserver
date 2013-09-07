package com.google.code.jstringserver.performance;

public class ReturnMessage {

    private static final String TO_RETURN = "OK";
    
    byte[] messageToWriteNext(int writtenSoFar) {
        int length = TO_RETURN.length();
        if (writtenSoFar == length) {
            return null;
        }
        return TO_RETURN.substring(writtenSoFar, length - writtenSoFar).getBytes();
    }
    
    boolean isWritingComplete(int writtenSoFar) {
        return !(writtenSoFar == TO_RETURN.length());
    }
}
