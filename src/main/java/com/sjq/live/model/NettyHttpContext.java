package com.sjq.live.model;

import io.netty.channel.ChannelHandlerContext;

public class NettyHttpContext {

    private ChannelHandlerContext ctx;
    private NettyHttpRequest nettyHttpRequest;

    public NettyHttpContext(ChannelHandlerContext ctx, NettyHttpRequest nettyHttpRequest) {
        this.ctx = ctx;
        this.nettyHttpRequest = nettyHttpRequest;
    }

    public NettyHttpRequest getHttpRequest() {
        return nettyHttpRequest;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }


}
