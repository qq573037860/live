package com.sjq.live.model;

import com.sjq.live.support.netty.NettyChannelAttribute;
import io.netty.channel.ChannelHandlerContext;

public class NettyHttpContext {

    private ChannelHandlerContext ctx;

    public NettyHttpContext(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public NettyHttpRequest getHttpRequest() {
        return NettyChannelAttribute.getNettyHttpRequest(ctx);
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public void setCtx(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }


}
