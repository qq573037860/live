package com.sjq.live.support;

public abstract class AbstractLiveStreamHandler {
    private String id;
    private volatile boolean isFirst = true;

    /**
     *  发送flvheader + keyframe
     * @param bytes
     */
    public void send(byte[] bytes) {
        if (isFirst) {
            isFirst = false;
            onData(bytes);
        }
    };

    public void send(byte[] bytes, byte[] headerData, boolean isTagHeaderStart) {
        if (isFirst) {//第一次发送的起始数据(包含flvHeader 和 keyFrames)
            synchronized (this) {
                if (isFirst) {
                    isFirst = false;
                    onData(headerData);
                    if (!isTagHeaderStart) {//要从一个tagHeader开始读数据
                        return;
                    }
                }
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
