package com.sjq.live.endpoint.netty.bootstrap;

import com.sjq.live.model.LiveException;
import com.sjq.live.model.NettyHttpContext;
import com.sjq.live.model.NettyHttpRequest;
import com.sjq.live.support.netty.NettyChannelAttribute;
import com.sjq.live.support.netty.NettyInputStreamProcessor;
import com.sjq.live.support.netty.NettyOutputStream;
import com.sjq.live.utils.NettyUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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

public class NettyHttpHandler extends AbstractNettyHandler {

    private static final Logger logger = LoggerFactory.getLogger(NettyHttpHandler.class);

    private NettyHttpEndPointHandlerProxy nettyHttpEndPointHandlerProxy;
    private NettyHttpRequest nettyHttpRequest;
    private boolean isNotFounded = false;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof DefaultHttpRequest) {
            final DefaultHttpRequest request = (DefaultHttpRequest) msg;
            processHttpRequest(request, ctx);
        } else if (msg instanceof DefaultHttpContent) {
            final DefaultHttpContent defaultHttpContent = (DefaultHttpContent) msg;
            processChunkedHttpContent(defaultHttpContent);
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
        final String hostAddress = NettyChannelAttribute.getHostAddress(ctx);
        if (Objects.nonNull(nettyHttpRequest) && nettyHttpRequest.isChunkedReq()) {
            nettyHttpRequest.getChunkDataHandler().exceptionOccurred(new LiveException(String.format("客户端[%s]断开连接", hostAddress)));
        }
    }

    @Override
    public void doExceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        //触发异常信息
        final String hostAddress = NettyChannelAttribute.getHostAddress(ctx);
        if (Objects.nonNull(nettyHttpRequest) && nettyHttpRequest.isChunkedReq()) {
            nettyHttpRequest.getChunkDataHandler().exceptionOccurred(new LiveException(String.format("客户端[%s]连接异常", hostAddress), cause));
        }
    }

    private void processHttpRequest(DefaultHttpRequest request, ChannelHandlerContext ctx) throws URISyntaxException {
        //不处理websocket请求
        if (NettyUtils.isWebsocketRequest(request.headers())) {
            ctx.fireChannelRead(request);
            return;
        }

        try {
            processNormalHttpRequest(request, ctx);
        } finally {
            ReferenceCountUtil.release(request);
        }
    }

    private void processNormalHttpRequest(DefaultHttpRequest request, ChannelHandlerContext ctx) throws URISyntaxException {
        // 获取请求参数
        final QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
        final Map<String, Object> params = NettyUtils.convertParams(queryStringDecoder.parameters());
        final String path = new URI(request.uri()).getPath();
        final HttpHeaders headers = request.headers();
        final boolean isFullHttpRequest = request instanceof DefaultFullHttpRequest;

        //注册endPoint
        nettyHttpEndPointHandlerProxy = NettyEndPointRegister.match(path, HttpMethod.resolve(request.method().name()));

        //返回404
        if (Objects.isNull(nettyHttpEndPointHandlerProxy)) {
            isNotFounded = true;
            NettyUtils.writeHttpNotFoundResponse(ctx);
            ctx.flush();
            return;
        }

        //保存请求数据
        nettyHttpRequest = new NettyHttpRequest();
        nettyHttpRequest.setParams(params);
        nettyHttpRequest.setPath(path);
        nettyHttpRequest.setHttpVersion(request.protocolVersion());
        nettyHttpRequest.setChunkedReq(StringUtils.equals(headers.get(HttpHeaderNames.TRANSFER_ENCODING), HttpHeaderValues.CHUNKED));

        if (nettyHttpRequest.isChunkedReq()) {
            //如果是chunked请求,提前调用
            processChunkedRequest(ctx);
        } else if (isFullHttpRequest) {
            //如果是个完成请求，则直接调用
            processFullHttpRequest(ctx);
        }
    }

    private void processFullHttpRequest(final ChannelHandlerContext ctx) {
        nettyHttpEndPointHandlerProxy.invokeAsync(nettyHttpRequest, ctx);
    }

    private void processChunkedRequest(final ChannelHandlerContext ctx) {
        //保存chunkDataHandler
        nettyHttpRequest.setChunkDataHandler(new NettyInputStreamProcessor.ChunkDataHandler());
        //调用
        nettyHttpEndPointHandlerProxy.invokeAsync(nettyHttpRequest, ctx);
    }

    private void processLastChunkedRequest(final ByteBuf byteBuf) {
        //添加httpChunkContent
        final byte[] lastContent = byteBuf.array();
        if (lastContent.length > 0) {
            nettyHttpRequest.getChunkDataHandler().offer(lastContent);
        }
        //chunked数据接收完毕
        nettyHttpRequest.getChunkDataHandler().reachEnd();
    }

    private void processChunkedHttpContent(final DefaultHttpContent httpContent) {
        final ByteBuf byteBuf = httpContent.content();
        try {
            //添加httpChunkContent
            final NettyInputStreamProcessor.ChunkDataHandler chunkDataHandler = nettyHttpRequest.getChunkDataHandler();
            byte[] data = NettyUtils.convertToByteArr(byteBuf);
            chunkDataHandler.offer(data);
        } finally {
            ReferenceCountUtil.release(httpContent);
        }
    }

    private void processLastHttpContent(final LastHttpContent httpContent,
                                        final ChannelHandlerContext ctx) {
        if (isNotFounded) {
            return;
        }

        final ByteBuf byteBuf = httpContent.content();

        try {
            if (nettyHttpRequest.isChunkedReq()) {
                //处理最后一次chunked请求数据
                processLastChunkedRequest(byteBuf);
            } else {
                //处理Http普通请求
                processFullHttpRequest(ctx);
            }
        } finally {
            ReferenceCountUtil.release(httpContent);
        }
    }
}
