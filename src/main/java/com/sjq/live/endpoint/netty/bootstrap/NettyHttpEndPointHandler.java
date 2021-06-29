package com.sjq.live.endpoint.netty.bootstrap;


import com.sjq.live.model.NettyHttpContext;
import com.sjq.live.model.NettyHttpRequest;
import io.netty.channel.ChannelHandlerContext;

public interface NettyHttpEndPointHandler {

    void invoke(NettyHttpRequest nettyHttpRequest, ChannelHandlerContext ctx);

    void invokeAsync(NettyHttpRequest nettyHttpRequest, ChannelHandlerContext ctx);
}
