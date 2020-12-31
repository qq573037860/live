package com.sjq.live.endpoint.netty.core;

import com.sjq.live.model.LiveException;
import com.sjq.live.model.NettyRequestParam;
import com.sjq.live.support.netty.NettyInputStreamProcessor;
import com.sjq.live.support.netty.NettyOutputStreamProcessor;
import com.sjq.live.utils.IpAddressUtils;
import com.sjq.live.utils.NettyUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.AttributeKey;
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
            processHttpContent(defaultHttpContent);
        } else if (msg instanceof LastHttpContent) {
            LastHttpContent lastHttpContent = (LastHttpContent) msg;
            processLastHttpContent(lastHttpContent, ctx);
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

        //注册endPoint
        NettyEndPointRegister.MethodInvokerHandler methodInvokerHandler = NettyEndPointRegister.match(path, HttpMethod.resolve(request.method().name()));
        if (Objects.isNull(methodInvokerHandler)) {//返回404
            NettyUtils.writeHttpNotFoundResponse(ctx);
            return;
        }

        //保存请求数据
        THREAD_LOCAL.remove();
        NettyRequestParam nettyRequestParam = THREAD_LOCAL.get();
        nettyRequestParam.setPublishId(CollectionUtils.isEmpty(publishIds) ? null : publishIds.get(0));
        nettyRequestParam.setPath(path);
        nettyRequestParam.setChunkedReq(StringUtils.equals(request.headers().get(HttpHeaderNames.TRANSFER_ENCODING), HttpHeaderValues.CHUNKED));
        nettyRequestParam.setMethodInvokerHandler(methodInvokerHandler);

        //chunked请求,提前调用
        if (nettyRequestParam.isChunkedReq()) {
            //构建netty流处理器
            NettyInputStreamProcessor.ChunkDataHandler chunkDataHandler = new NettyInputStreamProcessor.ChunkDataHandler();
            NettyInputStreamProcessor nettyInputStreamProcessor = new NettyInputStreamProcessor(chunkDataHandler, ctx);
            //保存chunkDataHandler
            nettyRequestParam.setChunkDataHandler(chunkDataHandler);
            methodInvokerHandler.invoke(new Object[]{nettyRequestParam.getPublishId(), nettyInputStreamProcessor});
        }
    }

    private void processHttpContent(DefaultHttpContent defaultHttpContent) {
        NettyRequestParam nettyRequestParam = THREAD_LOCAL.get();
        //添加httpChunkContent
        NettyInputStreamProcessor.ChunkDataHandler chunkDataHandler = nettyRequestParam.getChunkDataHandler();
        chunkDataHandler.offer(defaultHttpContent.content().array());
    }

    private void processLastHttpContent(LastHttpContent lastHttpContent, ChannelHandlerContext ctx) {
        NettyRequestParam nettyRequestParam = THREAD_LOCAL.get();
        if (nettyRequestParam.isChunkedReq()) {
            //添加httpChunkContent
            byte[] lastContent = lastHttpContent.content().array();
            if (lastContent.length > 0) {
                nettyRequestParam.getChunkDataHandler().offer(lastContent);
            }
            //chunked数据接收完毕
            nettyRequestParam.getChunkDataHandler().reachEnd();
        } else {
            //处理Http普通请求
            nettyRequestParam.getMethodInvokerHandler().invoke(new Object[]{nettyRequestParam.getPublishId(), new NettyOutputStreamProcessor(ctx)});
        }
    }
}