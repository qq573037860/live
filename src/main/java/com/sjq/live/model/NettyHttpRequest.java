package com.sjq.live.model;

import com.sjq.live.support.netty.NettyInputStreamProcessor;
import io.netty.handler.codec.http.HttpVersion;

import java.util.Map;

public class NettyHttpRequest {

    private Map<String, Object> params;

    private String path;

    private boolean isChunkedReq;

    private boolean isKeepAlive;

    private HttpVersion httpVersion;

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

    public boolean isKeepAlive() {
        return isKeepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        isKeepAlive = keepAlive;
    }

    public NettyInputStreamProcessor.ChunkDataHandler getChunkDataHandler() {
        return chunkDataHandler;
    }

    public void setChunkDataHandler(NettyInputStreamProcessor.ChunkDataHandler chunkDataHandler) {
        this.chunkDataHandler = chunkDataHandler;
    }

    public HttpVersion getHttpVersion() {
        return httpVersion;
    }

    public void setHttpVersion(HttpVersion httpVersion) {
        this.httpVersion = httpVersion;
    }
}
