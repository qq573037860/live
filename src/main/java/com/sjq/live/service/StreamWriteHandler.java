package com.sjq.live.service;

import com.sjq.live.support.OutputStreamProcessor;

import java.util.function.Function;

public class StreamWriteHandler {

    private OutputStreamProcessor out;
    private Function closeCallBack;

    public StreamWriteHandler(OutputStreamProcessor out, Function closeCallBack) {
        this.out = out;
        this.closeCallBack = closeCallBack;
    }

    public void write(byte[] bytes) {
        out.write(bytes);
    }

    public void close() {
        closeCallBack.apply(null);
        out.close();
    }

}
