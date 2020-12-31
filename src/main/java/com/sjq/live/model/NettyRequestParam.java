package com.sjq.live.model;

import com.sjq.live.endpoint.netty.core.NettyEndPointRegister;
import com.sjq.live.support.netty.NettyInputStreamProcessor;

public class NettyRequestParam {

    private String publishId;

    private String path;

    private boolean isChunkedReq;

    private NettyInputStreamProcessor.ChunkDataHandler chunkDataHandler;

    private NettyEndPointRegister.MethodInvokerHandler methodInvokerHandler;

    public String getPublishId() {
        return publishId;
    }

    public void setPublishId(String publishId) {
        this.publishId = publishId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isChunkedReq() {
        return isChunkedReq;
    }

    public void setChunkedReq(boolean chunkedReq) {
        isChunkedReq = chunkedReq;
    }

    public NettyInputStreamProcessor.ChunkDataHandler getChunkDataHandler() {
        return chunkDataHandler;
    }

    public void setChunkDataHandler(NettyInputStreamProcessor.ChunkDataHandler chunkDataHandler) {
        this.chunkDataHandler = chunkDataHandler;
    }

    public NettyEndPointRegister.MethodInvokerHandler getMethodInvokerHandler() {
        return methodInvokerHandler;
    }

    public void setMethodInvokerHandler(NettyEndPointRegister.MethodInvokerHandler methodInvokerHandler) {
        this.methodInvokerHandler = methodInvokerHandler;
    }
}
