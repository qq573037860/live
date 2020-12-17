package com.sjq.live.model;

public class LiveException extends Exception {

    public LiveException() {
        super();
    }

    public LiveException(String message) {
        super(message);
    }

    public LiveException(Throwable cause) {
        super(cause);
    }

    public LiveException(String message, Throwable cause) {
        super(message, cause);
    }

}
