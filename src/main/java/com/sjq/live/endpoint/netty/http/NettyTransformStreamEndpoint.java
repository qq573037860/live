package com.sjq.live.endpoint.netty.http;

import com.sjq.live.constant.LiveConfiguration;
import com.sjq.live.endpoint.netty.NettyEndPointSwitch;
import com.sjq.live.endpoint.netty.bootstrap.NettyEndPoint;
import com.sjq.live.model.NettyHttpContext;
import com.sjq.live.handler.TransformStreamHandler;
import com.sjq.live.support.netty.NettyInputStreamProcessor;
import com.sjq.live.support.netty.NettyOutputStreamProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.Map;

@NettyEndPoint
@Component
@ConditionalOnBean(NettyEndPointSwitch.class)
public class NettyTransformStreamEndpoint {

    @Autowired
    private TransformStreamHandler transformStreamHandler;

    /**
     * 原始流(供ffmpeg调用)
     * @param context
     * @throws Exception
     */
    @NettyEndPoint(path = LiveConfiguration.ORIGIN_STREAM_PATH, method = HttpMethod.GET)
    public void originStream(final NettyHttpContext context) throws Exception {
        final Map<String, Object> params = context.getHttpRequest().getParams();
        transformStreamHandler.processOriginalStream((String) params.get("publishId"), new NettyOutputStreamProcessor(context.getOutputStream()));
    }

    /**
     * 经ffmpeg转换后的流(供ffmpeg调用)
     * @param context
     * @throws Exception
     */
    @NettyEndPoint(path = LiveConfiguration.TRANSFORMED_STREAM_PATH, method = HttpMethod.POST)
    public void transformedStream(final NettyHttpContext context) throws Exception {
        final Map<String, Object> params = context.getHttpRequest().getParams();
        transformStreamHandler.processTransformedStream((String) params.get("publishId"), new NettyInputStreamProcessor(context));
    }

}
