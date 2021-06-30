package com.sjq.live.endpoint.netty.bootstrap;

import com.sjq.live.constant.LiveConfiguration;
import com.sjq.live.model.NettyWebsocketContext;
import com.sjq.live.model.NettyWebsocketRequest;
import com.sjq.live.utils.NettyUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

public class NettyWebsocketHandler extends AbstractNettyHandler {

    private static final Logger logger = LoggerFactory.getLogger(NettyWebsocketHandler.class);

    private LiveConfiguration configuration;

    private NettyWebsocketContext context;
    private NettyWebsocketEndPointHandlerProxy methodInvokerHandler;
    private WebSocketServerHandshaker handshake;

    private boolean isBinary = true;

    public NettyWebsocketHandler(final LiveConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof DefaultHttpRequest) {
            DefaultHttpRequest request = (DefaultHttpRequest) msg;
            processHttpRequest(request, ctx);
        } else if (msg instanceof WebSocketFrame) {
            WebSocketFrame webSocketFrame = (WebSocketFrame) msg;
            processWebSocketFrame(webSocketFrame, ctx);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void doChannelInactive(ChannelHandlerContext ctx) {
    }

    @Override
    public void doExceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    }

    private void processHttpRequest(DefaultHttpRequest request, ChannelHandlerContext ctx) throws URISyntaxException {
        if (!NettyUtils.isWebsocketRequest(request.headers())) {
            ctx.fireChannelRead(request);
            return;
        }

        // 构造请求
        final NettyWebsocketRequest websocketRequest = new NettyWebsocketRequest();
        websocketRequest.setAttribute(NettyUtils.convertParams(new QueryStringDecoder(request.uri()).parameters()));
        websocketRequest.setPath(request.uri());
        context = new NettyWebsocketContext(ctx, websocketRequest);

        // 处理请求
        try {
            processWebsocketRequest(new URI(request.uri()).getPath(), request, ctx);
        } finally {
            ReferenceCountUtil.release(request);
        }
    }

    private void processWebsocketRequest(final String path,
                                         final DefaultHttpRequest request,
                                         final ChannelHandlerContext ctx) {
        //注册endPoint
        methodInvokerHandler = NettyEndPointRegister.match(path);

        //返回404
        if (Objects.isNull(methodInvokerHandler)) {
            NettyUtils.writeHttpNotFoundResponse(ctx);
            ctx.flush();
            return;
        }

        //握手
        if (!methodInvokerHandler.beforeHandshake(context)) {
            NettyUtils.writeHttpBadRequestResponse(ctx);
            ctx.flush();
            return;
        }

        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(buildUrl(path), null, false);
        handshake = wsFactory.newHandshaker(request);

        if (Objects.isNull(handshake)) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            return;
        }

        ChannelFuture future = handshake.handshake(ctx.channel(), request);
        //链接成功回调
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) {
                methodInvokerHandler.afterConnectionEstablished(context);
                channelFuture.removeListener(this);
            }
        });
    }

    private String buildUrl(final String path) {
        return String.format("%s://%s:%s%s", LiveConfiguration.WEBSOCKET_PROTOCOL, configuration.getServerIp(), configuration.getServerPort(), path);
    }

    private void processWebSocketFrame(final WebSocketFrame frame,
                                       final ChannelHandlerContext ctx){
        if (frame instanceof CloseWebSocketFrame) {
            // 处理关闭链路的指令
            handshake.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            methodInvokerHandler.afterConnectionClosed(context);
        } else if (frame instanceof PingWebSocketFrame) {
            // 处理ping消息
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
        } else {
            try {
                if (frame instanceof TextWebSocketFrame) {
                    isBinary = false;
                    TextWebSocketFrame socketFrame = ((TextWebSocketFrame) frame);
                    methodInvokerHandler.handleMessage(context, socketFrame.text(), socketFrame.isFinalFragment());
                } else if (frame instanceof BinaryWebSocketFrame) {
                    isBinary = true;
                    BinaryWebSocketFrame socketFrame = (BinaryWebSocketFrame) frame;
                    methodInvokerHandler.handleMessage(context, NettyUtils.convertToByteArr(socketFrame.content()), socketFrame.isFinalFragment());
                } else if (frame instanceof ContinuationWebSocketFrame) {
                    ContinuationWebSocketFrame socketFrame = (ContinuationWebSocketFrame) frame;
                    if (isBinary) {
                        methodInvokerHandler.handleMessage(context, NettyUtils.convertToByteArr(socketFrame.content()), socketFrame.isFinalFragment());
                    } else {
                        methodInvokerHandler.handleMessage(context, socketFrame.text(), socketFrame.isFinalFragment());
                    }
                }
            } finally {
                ReferenceCountUtil.release(frame);
            }
        }
    }
}
