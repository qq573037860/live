package com.sjq.live.utils;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

public class NettyUtils {

    public static void writeHttpOkResponse(final ChannelHandlerContext ctx) {
        writeSimpleHttpResponse(ctx, HttpResponseStatus.OK);
    }

    public static void writeHttpNotFoundResponse(final ChannelHandlerContext ctx) {
        writeSimpleHttpResponse(ctx, HttpResponseStatus.NOT_FOUND);
    }

    public static void writeSimpleHttpResponse(final ChannelHandlerContext ctx,
                                               final HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        response.headers().set("Content-Length", 0);
        ctx.write(response);
    }

    public static void writeHttpChunkResponse(final ChannelHandlerContext ctx) {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
        HttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, headers);
        ctx.write(resp);
    }

    public static void writeChunkContentResponse(final ChannelHandlerContext ctx,
                                                 final byte[] data) {
        DefaultHttpContent chunk = new DefaultHttpContent(Unpooled.wrappedBuffer(data));
        ctx.write(chunk);
    }

    public static void wirteLastEmptyContentResponse(final ChannelHandlerContext ctx) {
        ctx.write(LastHttpContent.EMPTY_LAST_CONTENT);
    }

}
