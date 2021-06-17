package com.sjq.live.support;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class AbstractStreamDistributeHandler {
    private String id;
    private volatile int status = 0;

    private Consumer<String> destroyCallBack;

    protected AbstractStreamDistributeHandler() {
        this.id = UUID.randomUUID().toString();
    }

    /**
     *  发送flvheader + keyframe
     * @param bytes
     */
    public void send(final byte[] bytes) {
        if (status == 0) {
            status = 1;
            onData(bytes);
        }
    }

    public void send(final byte[] bytes,
                     final byte[] headerData,
                     final boolean isTagHeaderStart) {

        if (status == 2) {
            onData(bytes);
            return;
        }

        //第一次发送的起始数据(包含flvHeader 和 keyFrames)
        if (status == 0) {
            synchronized (this) {
                send(headerData);
            }
        }
        if (status == 1) {
            //要从一个tagHeader开始读数据
            if (!isTagHeaderStart) {
                return;
            }

            onData(bytes);
            status = 2;
        }
    }

    public void destroy() {
        if (Objects.nonNull(destroyCallBack)) {
            destroyCallBack.accept(id);
        }
    }

    public Consumer<String> getDestroyCallBack() {
        return destroyCallBack;
    }

    public void setDestroyCallBack(Consumer<String> destroyCallBack) {
        this.destroyCallBack = destroyCallBack;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    protected abstract void onData(final byte[] bytes);
}
