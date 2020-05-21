package com.sjq.live.support;

public abstract class AbstractLiveStreamHandler {
    private String id;
    private volatile boolean isFirst = true;

    public void send(byte[] bytes, byte[] headerData, boolean isTagHeaderStart) {
        if (isFirst) {//第一次发送的起始数据(包含flvHeader 和 keyFrames)
            if (isTagHeaderStart) {//要从一个tagHeader开始读数据
                isFirst = false;
                onData(headerData);
            } else {
                return;
            }
        }
        onData(bytes);
    }

    protected abstract void onData(byte[] bytes);

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
