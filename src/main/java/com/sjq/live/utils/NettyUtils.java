package com.sjq.live.utils;

import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

public class NettyUtils {

    public static void writeHttpOkResponse(final ChannelHandlerContext ctx) {
        writeSimpleHttpResponse(ctx, HttpResponseStatus.OK);
    }

    public static void writeHttpNotFoundResponse(final ChannelHandlerContext ctx) {
        writeSimpleHttpResponse(ctx, HttpResponseStatus.NOT_FOUND);
    }

    public static void writeHttpBadRequestResponse(final ChannelHandlerContext ctx) {
        writeSimpleHttpResponse(ctx, HttpResponseStatus.BAD_REQUEST);
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

    public static void writeLastEmptyContentResponse(final ChannelHandlerContext ctx) {
        ctx.write(LastHttpContent.EMPTY_LAST_CONTENT);
    }

    public static void writeContinueResponse(final ChannelHandlerContext ctx) {
        ctx.write(new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
    }

    public static Map<String, Object> convertParams(final Map<String, List<String>> listMap) {
        final Map<String, Object> params = Maps.newHashMap();
        for (Map.Entry<String, List<String>> entry : listMap.entrySet()) {
            if (!CollectionUtils.isEmpty(entry.getValue())) {
                params.put(entry.getKey(), entry.getValue().size() > 1 ? entry.getValue() : entry.getValue().get(0));
            }
        }
        return params;
    }

    public static boolean isWebsocketRequest(final HttpHeaders headers) {
        return StringUtils.equals("websocket", headers.get("Upgrade"));
    }

    public static byte[] convertToByteArr(final ByteBuf byteBuf) {
        byte[] data;
        if (byteBuf.isDirect()) {
            data = new byte[byteBuf.readableBytes()];
            byteBuf.getBytes(byteBuf.readerIndex(), data);
        } else {
            data = byteBuf.nioBuffer().array();
        }
        return data;
    }
}
