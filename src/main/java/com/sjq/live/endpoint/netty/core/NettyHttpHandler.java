package com.sjq.live.endpoint.netty.core;

import com.sjq.live.model.LiveException;
import com.sjq.live.model.NettyRequestParam;
import com.sjq.live.support.netty.NettyInputStreamProcessor;
import com.sjq.live.support.netty.NettyOutputStreamProcessor;
import com.sjq.live.utils.IpAddressUtils;
import com.sjq.live.utils.NettyUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.FastThreadLocal;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.util.CollectionUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

public class NettyHttpHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(NettyHttpHandler.class);

    private static final FastThreadLocal<NettyRequestParam> THREAD_LOCAL = new FastThreadLocal<NettyRequestParam>() {
        @Override
        protected NettyRequestParam initialValue() {
            return new NettyRequestParam();
        }
    };

    private static final AttributeKey<String> hostAddressAttributeKey = AttributeKey.valueOf("hostAddress");

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String hostAddress = IpAddressUtils.getSocketIpPortInfo(ctx);
        ctx.channel().attr(hostAddressAttributeKey).set(hostAddress);
        logger.info("客户端[{}]成功连接服务器", hostAddress);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof DefaultHttpRequest) {
            DefaultHttpRequest request = (DefaultHttpRequest) msg;
            processHttpRequest(request, ctx);
        } else if (msg instanceof DefaultHttpContent) {
            DefaultHttpContent defaultHttpContent = (DefaultHttpContent) msg;
            processChunkedHttpContent(defaultHttpContent);
        } else if (msg instanceof LastHttpContent) {
            LastHttpContent lastHttpContent = (LastHttpContent) msg;
            processLastHttpContent(lastHttpContent, ctx);
        } else if (msg instanceof WebSocketFrame) {
            WebSocketFrame webSocketFrame = (WebSocketFrame) msg;

        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //触发异常信息
        NettyRequestParam nettyRequestParam = THREAD_LOCAL.get();
        String hostAddress = ctx.channel().attr(hostAddressAttributeKey).get();
        logger.info("客户端[{}]与服务器断开连接", hostAddress);
        if (nettyRequestParam.isChunkedReq()) {
            nettyRequestParam.getChunkDataHandler().exceptionOccurred(new LiveException(String.format("客户端[%s]断开连接", hostAddress)));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        //触发异常信息
        NettyRequestParam nettyRequestParam = THREAD_LOCAL.get();
        String hostAddress = ctx.channel().attr(hostAddressAttributeKey).get();
        logger.info("客户端[{}]与服务器连接异常", hostAddress);
        if (nettyRequestParam.isChunkedReq()) {
            nettyRequestParam.getChunkDataHandler().exceptionOccurred(new LiveException(String.format("客户端[%s]连接异常", hostAddress), cause));
        }
    }

    private void processHttpRequest(DefaultHttpRequest request, ChannelHandlerContext ctx) throws URISyntaxException {
        // 获取请求参数
        final QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
        final List<String> publishIds = queryStringDecoder.parameters().get("publishId");
        final String path = new URI(request.uri()).getPath();
        HttpHeaders headers = request.headers();
        final ByteBuf content = request instanceof DefaultFullHttpRequest ? ((DefaultFullHttpRequest)request).content() : null;

        try {
            if (StringUtils.equals("websocket", request.headers().get("Upgrade"))) {//websocket 请求
                processWebsocketRequest(request, ctx.channel());
            } else {
                processNormalHttpRequest(ctx, path, request.method().name(), publishIds, headers, request instanceof DefaultFullHttpRequest);
            }
        } finally {
            if (Objects.nonNull(content)) {
                ReferenceCountUtil.release(content);
            }
        }
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
                                          List<String> publishIds,
                                          HttpHeaders headers,
                                          boolean isFullHttpRequest) {
        //注册endPoint
        NettyEndPointRegister.MethodInvokerHandler methodInvokerHandler = NettyEndPointRegister.match(path, HttpMethod.resolve(httpMethod));
        if (Objects.isNull(methodInvokerHandler)) {//返回404
            NettyUtils.writeHttpNotFoundResponse(ctx);
            return;
        }

        //保存请求数据
        THREAD_LOCAL.remove();
        NettyRequestParam nettyRequestParam = THREAD_LOCAL.get();
        nettyRequestParam.setPublishId(CollectionUtils.isEmpty(publishIds) ? null : publishIds.get(0));
        nettyRequestParam.setPath(path);
        nettyRequestParam.setChunkedReq(StringUtils.equals(headers.get(HttpHeaderNames.TRANSFER_ENCODING), HttpHeaderValues.CHUNKED));
        nettyRequestParam.setMethodInvokerHandler(methodInvokerHandler);

        if (nettyRequestParam.isChunkedReq()) {
            //如果是chunked请求,提前调用
            processChunkedRequest(ctx, nettyRequestParam, methodInvokerHandler);
        } else if (isFullHttpRequest) {
            //如果是个完成请求，则直接调用
            processFullHttpRequest(nettyRequestParam, ctx);
        }
    }

    private void processFullHttpRequest(NettyRequestParam nettyRequestParam, ChannelHandlerContext ctx) {
        nettyRequestParam.getMethodInvokerHandler().invoke(new Object[]{nettyRequestParam.getPublishId(), new NettyOutputStreamProcessor(ctx)});
    }

    private void processChunkedRequest(ChannelHandlerContext ctx,
                                       NettyRequestParam nettyRequestParam,
                                       NettyEndPointRegister.MethodInvokerHandler methodInvokerHandler) {
        //构建netty流处理器
        NettyInputStreamProcessor.ChunkDataHandler chunkDataHandler = new NettyInputStreamProcessor.ChunkDataHandler();
        NettyInputStreamProcessor nettyInputStreamProcessor = new NettyInputStreamProcessor(chunkDataHandler, ctx);
        //保存chunkDataHandler
        nettyRequestParam.setChunkDataHandler(chunkDataHandler);
        methodInvokerHandler.invokeAsyn(new Object[]{nettyRequestParam.getPublishId(), nettyInputStreamProcessor});
    }

    private void processLastChunkedRequest(ByteBuf byteBuf, NettyRequestParam nettyRequestParam) {
        //添加httpChunkContent
        byte[] lastContent = byteBuf.array();
        if (lastContent.length > 0) {
            nettyRequestParam.getChunkDataHandler().offer(lastContent);
        }
        //chunked数据接收完毕
        nettyRequestParam.getChunkDataHandler().reachEnd();
    }

    private void processChunkedHttpContent(DefaultHttpContent defaultHttpContent) {
        NettyRequestParam nettyRequestParam = THREAD_LOCAL.get();
        ByteBuf byteBuf = defaultHttpContent.content();
        try {
            //添加httpChunkContent
            NettyInputStreamProcessor.ChunkDataHandler chunkDataHandler = nettyRequestParam.getChunkDataHandler();
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
        NettyRequestParam nettyRequestParam = THREAD_LOCAL.get();
        ByteBuf byteBuf = lastHttpContent.content();
        try {
            if (nettyRequestParam.isChunkedReq()) {
                //处理最后一次chunked请求数据
                processLastChunkedRequest(byteBuf, nettyRequestParam);
            } else {
                //处理Http普通请求
                processFullHttpRequest(nettyRequestParam, ctx);
            }
        } finally {
            ReferenceCountUtil.release(byteBuf);
        }
    }
}
