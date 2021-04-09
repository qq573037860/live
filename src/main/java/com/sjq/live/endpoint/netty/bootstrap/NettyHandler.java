package com.sjq.live.endpoint.netty.bootstrap;

import com.google.common.collect.Maps;
import com.sjq.live.model.LiveException;
import com.sjq.live.model.NettyHttpContext;
import com.sjq.live.model.NettyHttpRequest;
import com.sjq.live.support.netty.NettyChannelAttribute;
import com.sjq.live.support.netty.NettyInputStreamProcessor;
import com.sjq.live.utils.IpAddressUtils;
import com.sjq.live.utils.NettyUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NettyHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(NettyHandler.class);

    /*private static final FastThreadLocal<NettyRequestParam> THREAD_LOCAL = new FastThreadLocal<NettyRequestParam>() {
        @Override
        protected NettyRequestParam initialValue() {
            return new NettyRequestParam();
        }
    };*/

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String hostAddress = IpAddressUtils.getSocketIpPortInfo(ctx);
        NettyChannelAttribute.setHostAddress(ctx, hostAddress);
        logger.info("客户端[{}]成功连接服务器", hostAddress);
        ctx.fireChannelActive();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof DefaultHttpRequest) {
            DefaultHttpRequest request = (DefaultHttpRequest) msg;
            processHttpRequest(request, ctx);
        } else if (msg instanceof DefaultHttpContent) {
            DefaultHttpContent defaultHttpContent = (DefaultHttpContent) msg;
            processChunkedHttpContent(defaultHttpContent, ctx);
        } else if (msg instanceof LastHttpContent) {
            LastHttpContent lastHttpContent = (LastHttpContent) msg;
            processLastHttpContent(lastHttpContent, ctx);
        } else if (msg instanceof WebSocketFrame) {
            WebSocketFrame webSocketFrame = (WebSocketFrame) msg;

        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String hostAddress = NettyChannelAttribute.getHostAddress(ctx);
        logger.info("客户端[{}]与服务器断开连接", hostAddress);
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        String hostAddress = NettyChannelAttribute.getHostAddress(ctx);
        logger.info("客户端[{}]与服务器连接异常", hostAddress, cause);
        ctx.fireExceptionCaught(cause);
    }

    private void processHttpRequest(DefaultHttpRequest request, ChannelHandlerContext ctx) throws URISyntaxException {
        // 获取请求参数
        final QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
        final Map<String, Object> params = buildParam(queryStringDecoder.parameters());
        final String path = new URI(request.uri()).getPath();
        HttpHeaders headers = request.headers();
        final ByteBuf content = request instanceof DefaultFullHttpRequest ? ((DefaultFullHttpRequest)request).content() : null;

        try {
            if (StringUtils.equals("websocket", request.headers().get("Upgrade"))) {//websocket 请求
                processWebsocketRequest(request, ctx.channel());
            } else {
                processNormalHttpRequest(ctx, path, request.method().name(), params, headers, request instanceof DefaultFullHttpRequest);
            }
        } finally {
            if (Objects.nonNull(content)) {
                ReferenceCountUtil.release(content);
            }
        }
    }

    private static Map<String, Object> buildParam(Map<String, List<String>> listMap) {
        final Map<String, Object> params = Maps.newHashMap();
        for (Map.Entry<String, List<String>> entry : listMap.entrySet()) {
            if (!CollectionUtils.isEmpty(entry.getValue())) {
                params.put(entry.getKey(), entry.getValue().size() > 1 ? entry.getValue() : entry.getValue().get(0));
            }
        }
        return params;
    }

    private void processWebsocketRequest(DefaultHttpRequest req, Channel channel) {
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                "ws://localhost:8081/websocket", null, false);
        WebSocketServerHandshaker handshake = wsFactory.newHandshaker(req);
        if (handshake == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(channel);
        } else {
            handshake.handshake(channel, req);
        }
    }

    private void processWebSocketFrame(WebSocketFrame frame, ChannelHandlerContext ctx){
        // 判断是否关闭链路的指令
        if (frame instanceof CloseWebSocketFrame) {
            //handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        // 判断是否ping消息
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(
                    new PongWebSocketFrame(frame.content().retain()));
            return;
        }
    }

    private void processNormalHttpRequest(ChannelHandlerContext ctx,
                                          String path,
                                          String httpMethod,
                                          Map<String, Object> params,
                                          HttpHeaders headers,
                                          boolean isFullHttpRequest) {
        //注册endPoint
        MethodInvokerHandler methodInvokerHandler = NettyEndPointRegister.match(path, HttpMethod.resolve(httpMethod));
        if (Objects.isNull(methodInvokerHandler)) {//返回404
            NettyUtils.writeHttpNotFoundResponse(ctx);
            return;
        }

        //保存请求数据
        NettyHttpRequest nettyHttpRequest = new NettyHttpRequest();
        nettyHttpRequest.setParams(params);
        nettyHttpRequest.setPath(path);
        nettyHttpRequest.setChunkedReq(StringUtils.equals(headers.get(HttpHeaderNames.TRANSFER_ENCODING), HttpHeaderValues.CHUNKED));
        NettyChannelAttribute.setMethodInvokerHandler(ctx, methodInvokerHandler);
        NettyChannelAttribute.setNettyHttpRequest(ctx, nettyHttpRequest);

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

    private void processChunkedRequest(ChannelHandlerContext ctx) {
        //保存chunkDataHandler
        NettyChannelAttribute.getNettyHttpRequest(ctx).setChunkDataHandler(new NettyInputStreamProcessor.ChunkDataHandler());
        //调用
        final MethodInvokerHandler methodInvokerHandler = NettyChannelAttribute.getMethodInvokerHandler(ctx);
        methodInvokerHandler.invokeAsync(new Object[]{new NettyHttpContext(ctx)});
    }

    private void processLastChunkedRequest(final ByteBuf byteBuf,
                                           final NettyHttpRequest nettyHttpRequest) {
        //添加httpChunkContent
        byte[] lastContent = byteBuf.array();
        if (lastContent.length > 0) {
            nettyHttpRequest.getChunkDataHandler().offer(lastContent);
        }
        //chunked数据接收完毕
        nettyHttpRequest.getChunkDataHandler().reachEnd();
    }

    private void processChunkedHttpContent(final DefaultHttpContent defaultHttpContent,
                                           final ChannelHandlerContext ctx) {
        NettyHttpRequest nettyHttpRequest = NettyChannelAttribute.getNettyHttpRequest(ctx);
        ByteBuf byteBuf = defaultHttpContent.content();
        try {
            //添加httpChunkContent
            NettyInputStreamProcessor.ChunkDataHandler chunkDataHandler = nettyHttpRequest.getChunkDataHandler();
            byte[] data;
            if (byteBuf.isDirect()) {
                data = new byte[byteBuf.readableBytes()];
                byteBuf.getBytes(byteBuf.readerIndex(), data);
            } else {
                data = byteBuf.nioBuffer().array();
            }
            chunkDataHandler.offer(data);
        } finally {
            ReferenceCountUtil.release(byteBuf);
        }
    }

    private void processLastHttpContent(LastHttpContent lastHttpContent, ChannelHandlerContext ctx) {
        NettyHttpRequest nettyHttpRequest = NettyChannelAttribute.getNettyHttpRequest(ctx);
        ByteBuf byteBuf = lastHttpContent.content();
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
