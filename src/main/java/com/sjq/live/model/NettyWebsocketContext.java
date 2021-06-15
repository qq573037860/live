package com.sjq.live.model;

import io.netty.channel.ChannelHandlerContext;

public class NettyWebsocketContext {

    private ChannelHandlerContext channelHandlerContext;
    private NettyWebsocketRequest nettyWebsocketRequest;

    public NettyWebsocketContext(ChannelHandlerContext channelHandlerContext, NettyWebsocketRequest nettyWebsocketRequest) {
        this.channelHandlerContext = channelHandlerContext;
        this.nettyWebsocketRequest = nettyWebsocketRequest;
    }

    public NettyWebsocketRequest getRequest() {
        return nettyWebsocketRequest;
    }

    public ChannelHandlerContext getChannelHandler() {
        return channelHandlerContext;
    }

    public void setChannelHandler(ChannelHandlerContext channelHandlerContext) {
        this.channelHandlerContext = channelHandlerContext;
    }


}
