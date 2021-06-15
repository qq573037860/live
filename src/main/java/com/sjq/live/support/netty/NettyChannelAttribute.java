package com.sjq.live.support.netty;

import com.sjq.live.model.NettyHttpRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.util.AttributeKey;

public class NettyChannelAttribute {

    private static final AttributeKey<String> HOST_ADDRESS_ATTRIBUTE_KEY = AttributeKey.valueOf("hostAddress");
    private static final AttributeKey<NettyHttpRequest> NETTY_HTTP_REQUEST_ATTRIBUTE_KEY = AttributeKey.valueOf("nettyHttpRequest");
    private static final AttributeKey<AbstractMethodInvokerHandler> METHOD_INVOKER_HANDLER = AttributeKey.valueOf("methodInvokerHandler");
    private static final AttributeKey<WebSocketServerHandshaker> WEBSOCKET_HAND_SHAKER = AttributeKey.valueOf("webSocketHandShaker");

    public static String getHostAddress(final ChannelHandlerContext ctx) {
        return ctx.channel().attr(HOST_ADDRESS_ATTRIBUTE_KEY).get();
    }

    public static void setHostAddress(final ChannelHandlerContext ctx,
                                      final String hostAddress) {
        ctx.channel().attr(HOST_ADDRESS_ATTRIBUTE_KEY).set(hostAddress);
    }

    public static NettyHttpRequest getNettyHttpRequest(final ChannelHandlerContext ctx) {
        return ctx.channel().attr(NETTY_HTTP_REQUEST_ATTRIBUTE_KEY).get();
    }

    public static void setNettyHttpRequest(final ChannelHandlerContext ctx,
                                           final NettyHttpRequest request) {
        ctx.channel().attr(NETTY_HTTP_REQUEST_ATTRIBUTE_KEY).set(request);
    }

    public static <T> T getMethodInvokerHandler(final ChannelHandlerContext ctx) {
        return (T) ctx.channel().attr(METHOD_INVOKER_HANDLER).get();
    }

    public static void setMethodInvokerHandler(final ChannelHandlerContext ctx,
                                               final AbstractMethodInvokerHandler methodInvokerHandler) {
        ctx.channel().attr(METHOD_INVOKER_HANDLER).set(methodInvokerHandler);
    }

    public static WebSocketServerHandshaker getWebSocketServerHandShaker(final ChannelHandlerContext ctx) {
        return ctx.channel().attr(WEBSOCKET_HAND_SHAKER).get();
    }

    public static void setWebSocketServerHandShaker(final ChannelHandlerContext ctx,
                                                    final WebSocketServerHandshaker handShaker) {
        ctx.channel().attr(WEBSOCKET_HAND_SHAKER).set(handShaker);
    }

    public static void clearAllAttribute(final ChannelHandlerContext ctx) {
        ctx.channel().attr(HOST_ADDRESS_ATTRIBUTE_KEY).set(null);
        ctx.channel().attr(NETTY_HTTP_REQUEST_ATTRIBUTE_KEY).set(null);
        ctx.channel().attr(METHOD_INVOKER_HANDLER).set(null);
        ctx.channel().attr(WEBSOCKET_HAND_SHAKER).set(null);
    }
}
