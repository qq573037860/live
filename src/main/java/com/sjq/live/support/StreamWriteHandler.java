package com.sjq.live.support;

public class StreamWriteHandler {

    private OutputStreamProcessor out;
    private StreamCloseCallBack closeCallBack;

    public StreamWriteHandler(OutputStreamProcessor out, StreamCloseCallBack callBack) {
        this.out = out;
        this.closeCallBack = callBack;
    }

    public void write(byte[] bytes) {
        out.write(bytes);
    }

    public void close() {
        out.close();
        closeCallBack.onClose();
    }

    /**
     * stream关闭回调方法
     */
    public interface StreamCloseCallBack {
        void onClose();
    }
}
