package com.google.code.jstringserver.performance;

public class ReturnMessage {

    private static final String TO_RETURN = "OK";
    
    String messageToWriteNext(int writtenSoFar) {
        int length = TO_RETURN.length();
        if (writtenSoFar == length) {
            return null;
        }
        return TO_RETURN.substring(writtenSoFar, length - writtenSoFar);
    }
    
    boolean isWritingComplete(int writtenSoFar) {
        return !(writtenSoFar == TO_RETURN.length());
    }
}
