package com.sjq.live.model;

import com.sjq.live.support.netty.NettyInputStreamProcessor;

import java.util.Map;

public class NettyHttpRequest {

    private Map<String, Object> params;

    private String path;

    private boolean isChunkedReq;

    private NettyInputStreamProcessor.ChunkDataHandler chunkDataHandler;

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
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
}
