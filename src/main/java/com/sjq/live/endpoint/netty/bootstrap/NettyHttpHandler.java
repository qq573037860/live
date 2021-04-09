package com.sjq.live.endpoint.netty.bootstrap;

import com.sjq.live.model.LiveException;
import com.sjq.live.model.NettyHttpContext;
import com.sjq.live.model.NettyHttpRequest;
import com.sjq.live.support.netty.NettyChannelAttribute;
import com.sjq.live.support.netty.NettyInputStreamProcessor;
import com.sjq.live.utils.NettyUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;

public class NettyHttpHandler extends AbstractNettyHttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(NettyHttpHandler.class);

    private MethodInvokerHandler methodInvokerHandler;
    private NettyHttpRequest nettyHttpRequest;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof DefaultHttpRequest) {
            final DefaultHttpRequest request = (DefaultHttpRequest) msg;
            processHttpRequest(request, ctx);
        } else if (msg instanceof DefaultHttpContent) {
            final DefaultHttpContent defaultHttpContent = (DefaultHttpContent) msg;
            processChunkedHttpContent(defaultHttpContent, ctx);
        } else if (msg instanceof LastHttpContent) {
            final LastHttpContent lastHttpContent = (LastHttpContent) msg;
            processLastHttpContent(lastHttpContent, ctx);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void doChannelInactive(ChannelHandlerContext ctx) {
        //触发异常信息
        final NettyHttpRequest nettyHttpRequest = NettyChannelAttribute.getNettyHttpRequest(ctx);
        final String hostAddress = NettyChannelAttribute.getHostAddress(ctx);
        if (Objects.nonNull(nettyHttpRequest) && nettyHttpRequest.isChunkedReq()) {
            nettyHttpRequest.getChunkDataHandler().exceptionOccurred(new LiveException(String.format("客户端[%s]断开连接", hostAddress)));
        }
    }

    @Override
    public void doExceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        //触发异常信息
        final NettyHttpRequest nettyHttpRequest = NettyChannelAttribute.getNettyHttpRequest(ctx);
        final String hostAddress = NettyChannelAttribute.getHostAddress(ctx);
        if (Objects.nonNull(nettyHttpRequest) && nettyHttpRequest.isChunkedReq()) {
            nettyHttpRequest.getChunkDataHandler().exceptionOccurred(new LiveException(String.format("客户端[%s]连接异常", hostAddress), cause));
        }
    }

    private void processHttpRequest(DefaultHttpRequest request, ChannelHandlerContext ctx) throws URISyntaxException {
        // 获取请求参数
        final QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
        final Map<String, Object> params = NettyUtils.convertParams(queryStringDecoder.parameters());
        final String path = new URI(request.uri()).getPath();
        final HttpHeaders headers = request.headers();
        final boolean isFullHttpRequest = request instanceof DefaultFullHttpRequest;
        final ByteBuf content = isFullHttpRequest ? ((DefaultFullHttpRequest)request).content() : null;

        if (NettyUtils.isWebsocketRequest(request.headers())) { //不处理websocket请求
            ctx.fireChannelRead(request);
        } else {
            try {
                processNormalHttpRequest(ctx, path, request.method().name(), params, headers, isFullHttpRequest);
            } finally {
                ReferenceCountUtil.release(content);
            }
        }
    }

    private void processNormalHttpRequest(final ChannelHandlerContext ctx,
                                          final String path,
                                          final String httpMethod,
                                          final Map<String, Object> params,
                                          final HttpHeaders headers,
                                          final boolean isFullHttpRequest) {
        //注册endPoint
        methodInvokerHandler = NettyEndPointRegister.match(path, HttpMethod.resolve(httpMethod));
        if (Objects.isNull(methodInvokerHandler)) {//返回404
            NettyUtils.writeHttpNotFoundResponse(ctx);
            return;
        }

        //保存请求数据
        nettyHttpRequest = new NettyHttpRequest();
        nettyHttpRequest.setParams(params);
        nettyHttpRequest.setPath(path);
        nettyHttpRequest.setChunkedReq(StringUtils.equals(headers.get(HttpHeaderNames.TRANSFER_ENCODING), HttpHeaderValues.CHUNKED));
        //NettyChannelAttribute.setMethodInvokerHandler(ctx, methodInvokerHandler);
        //NettyChannelAttribute.setNettyHttpRequest(ctx, nettyHttpRequest);

        if (nettyHttpRequest.isChunkedReq()) {
            //如果是chunked请求,提前调用
            processChunkedRequest(ctx);
        } else if (isFullHttpRequest) {
            //如果是个完成请求，则直接调用
            processFullHttpRequest(ctx);
        }
    }

    private void processFullHttpRequest(final ChannelHandlerContext ctx) {
        final MethodInvokerHandler methodInvokerHandler = NettyChannelAttribute.getMethodInvokerHandler(ctx);
        methodInvokerHandler.invoke(new Object[]{new NettyHttpContext(ctx)});
    }

    private void processChunkedRequest(final ChannelHandlerContext ctx) {
        //保存chunkDataHandler
        NettyChannelAttribute.getNettyHttpRequest(ctx).setChunkDataHandler(new NettyInputStreamProcessor.ChunkDataHandler());
        //调用
        final MethodInvokerHandler methodInvokerHandler = NettyChannelAttribute.getMethodInvokerHandler(ctx);
        methodInvokerHandler.invokeAsync(new Object[]{new NettyHttpContext(ctx)});
    }

    private void processLastChunkedRequest(final ByteBuf byteBuf,
                                           final NettyHttpRequest nettyHttpRequest) {
        //添加httpChunkContent
        final byte[] lastContent = byteBuf.array();
        if (lastContent.length > 0) {
            nettyHttpRequest.getChunkDataHandler().offer(lastContent);
        }
        //chunked数据接收完毕
        nettyHttpRequest.getChunkDataHandler().reachEnd();
    }

    private void processChunkedHttpContent(final DefaultHttpContent defaultHttpContent,
                                           final ChannelHandlerContext ctx) {
        final NettyHttpRequest nettyHttpRequest = NettyChannelAttribute.getNettyHttpRequest(ctx);
        final ByteBuf byteBuf = defaultHttpContent.content();
        try {
            //添加httpChunkContent
            final NettyInputStreamProcessor.ChunkDataHandler chunkDataHandler = nettyHttpRequest.getChunkDataHandler();
            byte[] data = NettyUtils.convertToByteArr(byteBuf);
            chunkDataHandler.offer(data);
        } finally {
            ReferenceCountUtil.release(byteBuf);
        }
    }

    private void processLastHttpContent(final LastHttpContent lastHttpContent,
                                        final ChannelHandlerContext ctx) {
        final NettyHttpRequest nettyHttpRequest = NettyChannelAttribute.getNettyHttpRequest(ctx);
        final ByteBuf byteBuf = lastHttpContent.content();
        try {
            if (nettyHttpRequest.isChunkedReq()) {
                //处理最后一次chunked请求数据
                processLastChunkedRequest(byteBuf, nettyHttpRequest);
            } else {
                //处理Http普通请求
                processFullHttpRequest(ctx);
            }
        } finally {
            ReferenceCountUtil.release(byteBuf);
        }
    }
}
