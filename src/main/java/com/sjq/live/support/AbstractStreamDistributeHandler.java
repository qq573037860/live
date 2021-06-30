package com.sjq.live.support;

import com.sjq.live.endpoint.original.websocket.OriginalSubscribeVideoStreamEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class AbstractStreamDistributeHandler {

    private static final Logger logger = LoggerFactory.getLogger(AbstractStreamDistributeHandler.class);

    private String id;
    private volatile int status = 0;

    /**
     * 非线程安全，初略统计即可
     */
    private int errorCount = 0;
    private static final int MAX_ERROR_COUNT = 10;

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
            doSend(bytes);
        }
    }

    public void send(final byte[] bytes,
                     final byte[] headerData,
                     final boolean isTagHeaderStart) {

        if (status == 2) {
            doSend(bytes);
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

            doSend(bytes);
            status = 2;
        }
    }

    private void doSend(final byte[] bytes) {
        try {
            if (errorCount < MAX_ERROR_COUNT) {
                onData(bytes);
            } else {
                logger.warn("StreamDistributeHandler[id:{}]连续发送异常超过{}次,之后执行将忽略", id, MAX_ERROR_COUNT);
            }
        } catch (Exception e) {
            //连续出错10次则关闭
            if (++errorCount > MAX_ERROR_COUNT) {
                destroy();
            }
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

    protected abstract void onData(final byte[] bytes) throws Exception;
}
