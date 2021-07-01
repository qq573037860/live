package com.sjq.live.support.netty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

public class NettyChannelAttribute {

    private static final AttributeKey<String> HOST_ADDRESS_ATTRIBUTE_KEY = AttributeKey.valueOf("hostAddress");

    private static final AttributeKey<ChannelFuture> LAST_CHANNEL_FUTURE = AttributeKey.valueOf("lastChannelFuture");

    public static String getHostAddress(final ChannelHandlerContext ctx) {
        return ctx.channel().attr(HOST_ADDRESS_ATTRIBUTE_KEY).get();
    }

    public static void setHostAddress(final ChannelHandlerContext ctx,
                                      final String hostAddress) {
        ctx.channel().attr(HOST_ADDRESS_ATTRIBUTE_KEY).set(hostAddress);
    }

    public static void setLastChannelFuture(final ChannelHandlerContext ctx, final ChannelFuture channelFuture) {
        ctx.channel().attr(LAST_CHANNEL_FUTURE).set(channelFuture);
    }

    public static ChannelFuture getLastChannelFuture(final ChannelHandlerContext ctx) {
        return ctx.channel().attr(LAST_CHANNEL_FUTURE).get();
    }

    public static void clearAllAttribute(final ChannelHandlerContext ctx) {
        ctx.channel().attr(HOST_ADDRESS_ATTRIBUTE_KEY).set(null);
        ctx.channel().attr(LAST_CHANNEL_FUTURE).set(null);
    }
}
