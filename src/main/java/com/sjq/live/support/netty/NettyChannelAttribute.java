package com.sjq.live.support.netty;

import com.sjq.live.model.NettyHttpRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.util.AttributeKey;

public class NettyChannelAttribute {

    private static final AttributeKey<String> HOST_ADDRESS_ATTRIBUTE_KEY = AttributeKey.valueOf("hostAddress");

    public static String getHostAddress(final ChannelHandlerContext ctx) {
        return ctx.channel().attr(HOST_ADDRESS_ATTRIBUTE_KEY).get();
    }

    public static void setHostAddress(final ChannelHandlerContext ctx,
                                      final String hostAddress) {
        ctx.channel().attr(HOST_ADDRESS_ATTRIBUTE_KEY).set(hostAddress);
    }

    public static void clearAllAttribute(final ChannelHandlerContext ctx) {
        ctx.channel().attr(HOST_ADDRESS_ATTRIBUTE_KEY).set(null);
    }
}
