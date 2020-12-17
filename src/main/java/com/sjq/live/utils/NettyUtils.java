package com.sjq.live.utils;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.util.Objects;

public class NettyUtils {

    public static void responseHttp(final byte[] responseBytes,
                                    final Integer contentLength,
                                    final ChannelHandlerContext ctx) {
        responseHttp(responseBytes, contentLength, ctx, true);
    }

    public static void responseHttp(final byte[] responseBytes,
                                    final Integer contentLength,
                                    final ChannelHandlerContext ctx,
                                    final boolean flush) {
        // 构造FullHttpResponse对象，FullHttpResponse包含message body
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(responseBytes));
        response.headers().set("Content-Type", "text/html; charset=utf-8");
        if (Objects.nonNull(contentLength)) {
            response.headers().set("Content-Length", contentLength);
        }

        if (flush) {
            ctx.writeAndFlush(response);
        } else {
            ctx.write(response);
        }
    }

}
