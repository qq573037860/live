package com.sjq.live.utils;

import com.google.common.collect.Maps;
import com.sjq.live.model.LiveException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedNioFile;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NettyUtils {

    public static void writeHttpOkResponse(final ChannelHandlerContext ctx) {
        writeSimpleHttpResponse(ctx, HttpResponseStatus.OK);
    }

    public static ChannelFuture writeHttpOkResponse(final ChannelHandlerContext ctx,
                                           final byte[] data) {
        return writeHttpResponse(ctx, HttpResponseStatus.OK, data);
    }

    public static ChannelFuture writeHttpOkResponse(final ChannelHandlerContext ctx,
                                                    final HttpVersion httpVersion,
                                                    final boolean isKeepAlive,
                                                    final String filePath) {
        File file;
        try {
            file = ResourceUtils.getFile("classpath:" + filePath);
        } catch (FileNotFoundException e) {
            return writeHttpNotFoundResponse(ctx);
        }

        // 写入文件头部
        writeFileHeaderResponse(ctx, httpVersion, isKeepAlive, file);

        // 写入文件内容
        writeFileBodyResponse(file, ctx);

        // 写入文件尾部
        return writeLastEmptyContentResponse(ctx);
    }

    public static void writeFileBodyResponse(final File file, final ChannelHandlerContext ctx) {
        RandomAccessFile accessFile;
        try {
            accessFile = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException e) {
            throw new LiveException(e);
        }

        FileChannel fileChannel = accessFile.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try {
            int length;
            while ((length = fileChannel.read(buffer)) != -1) {
                byte[] bytes = new byte[length];
                buffer.flip();
                buffer.get(bytes);
                buffer.clear();
                writeChunkContentResponse(ctx, bytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fileChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeFileHeaderResponse(final ChannelHandlerContext ctx,
                                               final HttpVersion httpVersion,
                                               final boolean isKeepAlive,
                                               final File file) {
        HttpResponse response = new DefaultHttpResponse(httpVersion, HttpResponseStatus.OK);

        // 设置文件格式内容
        if (file.getName().endsWith(".html")){
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        } else if(file.getName().endsWith(".js")){
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/x-javascript");
        } else if(file.getName().endsWith(".css")){
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/css; charset=UTF-8");
        }
        response.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);

        if (isKeepAlive) {

            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, file.length());
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderNames.KEEP_ALIVE);
        }

        ctx.write(response);
    }

    public static ChannelFuture writeHttpNotFoundResponse(final ChannelHandlerContext ctx) {
        return writeSimpleHttpResponse(ctx, HttpResponseStatus.NOT_FOUND);
    }

    public static void writeHttpBadRequestResponse(final ChannelHandlerContext ctx) {
        writeSimpleHttpResponse(ctx, HttpResponseStatus.BAD_REQUEST);
    }

    public static ChannelFuture writeSimpleHttpResponse(final ChannelHandlerContext ctx,
                                               final HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
        return ctx.write(response);
    }

    public static ChannelFuture writeHttpResponse(final ChannelHandlerContext ctx,
                                         final HttpResponseStatus status,
                                         final byte[] data) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.wrappedBuffer(data));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, 0);
        return ctx.write(response);
    }

    public static void writeHttpChunkResponse(final ChannelHandlerContext ctx) {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
        HttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, headers);
        ctx.write(resp);
    }

    public static void writeChunkContentResponse(final ChannelHandlerContext ctx,
                                                 final byte[] data) {
        if (Objects.isNull(data)) {
            writeLastEmptyContentResponse(ctx);
            return;
        }
        DefaultHttpContent chunk = new DefaultHttpContent(Unpooled.wrappedBuffer(data));
        ctx.write(chunk);
    }

    public static ChannelFuture writeLastEmptyContentResponse(final ChannelHandlerContext ctx) {
        return ctx.write(LastHttpContent.EMPTY_LAST_CONTENT);
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
