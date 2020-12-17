package com.sjq.live.endpoint.netty;

import com.sjq.live.constant.LiveConfiguration;
import com.sjq.live.endpoint.TransformStreamEndpointHook;
import com.sjq.live.support.netty.NettyOutputStreamProcessor;
import com.sjq.live.utils.NettyUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class NettyHttpHandler extends ChannelInboundHandlerAdapter {

    private final TransformStreamEndpointHook transformStreamEndpointHook;

    NettyHttpHandler(final TransformStreamEndpointHook transformStreamEndpointHook) {
        this.transformStreamEndpointHook = transformStreamEndpointHook;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws UnsupportedEncodingException, URISyntaxException {
        if (msg instanceof HttpRequest) {
            // 请求，解码器将请求转换成HttpRequest对象
            final HttpRequest request = (HttpRequest) msg;

            // 获取请求参数
            final QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
            final List<String> publishIds = queryStringDecoder.parameters().get("publishId");
            final String path = new URI(request.uri()).getPath();

            //处理相应
            if (CollectionUtils.isEmpty(publishIds)) {
                NettyUtils.responseHttp(new byte[0], 0, ctx);
            } else if (StringUtils.equals(path, LiveConfiguration.getOriginStreamPath())) {
                transformStreamEndpointHook.originStreamReach(publishIds.get(0), new NettyOutputStreamProcessor(ctx));
            } else if (StringUtils.equals(path, LiveConfiguration.getTransformedStreamPath())) {
                FullHttpRequest fullHttpRequest = (FullHttpRequest) request;
                //fullHttpRequest.content();
                //transformStreamEndpointHook.transformedStreamReach(publishIds.get(0), );
            } else {
                NettyUtils.responseHttp(new byte[0], 0, ctx);
            }
        }
    }

    public static void main(String[] args) {
        final QueryStringDecoder queryStringDecoder = new QueryStringDecoder("http://124.251.115.178:8787/wide-field/dist/w?a=1");
        System.out.println(queryStringDecoder.path());
        System.out.println(StringUtils.equals(queryStringDecoder.path(), "/wide-field/dist/w"));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
