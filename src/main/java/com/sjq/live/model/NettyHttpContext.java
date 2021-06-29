package com.sjq.live.model;

import com.sjq.live.support.netty.NettyOutputStream;

public class NettyHttpContext {

    private NettyOutputStream outputStream;
    private NettyHttpRequest nettyHttpRequest;

    public NettyHttpContext(NettyOutputStream outputStream, NettyHttpRequest nettyHttpRequest) {
        this.outputStream = outputStream;
        this.nettyHttpRequest = nettyHttpRequest;
    }

    public NettyHttpRequest getHttpRequest() {
        return nettyHttpRequest;
    }

    public NettyOutputStream getOutputStream() {
        return outputStream;
    }
}
