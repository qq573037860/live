package com.sjq.live.endpoint.netty;

import com.sjq.live.constant.LiveConfiguration;
import com.sjq.live.endpoint.AbstractTransformStreamEndpointHook;
import com.sjq.live.endpoint.netty.core.NettyEndPoint;
import com.sjq.live.support.InputStreamProcessor;
import com.sjq.live.support.OutputStreamProcessor;
import org.springframework.http.HttpMethod;

@NettyEndPoint
public class NettyTransformStreamEndpoint extends AbstractTransformStreamEndpointHook {

    /**
     * 原始流(供ffmpeg调用)
     * @param publishId
     * @param streamProcessor
     * @throws Exception
     */
    @NettyEndPoint(path = LiveConfiguration.ORIGIN_STREAM_PATH, method = HttpMethod.GET)
    public void originStream(final String publishId,
                             final OutputStreamProcessor streamProcessor) throws Exception {
        originStreamReach(publishId, streamProcessor);
    }

    /**
     * 经ffmpeg转换后的流(供ffmpeg调用)
     * @param publishId
     * @param streamProcessor
     * @throws Exception
     */
    @NettyEndPoint(path = LiveConfiguration.TRANSFORMED_STREAM_PATH, method = HttpMethod.POST)
    public void transformedStream(final String publishId,
                                  final InputStreamProcessor streamProcessor) throws Exception {
        transformedStreamReach(publishId, streamProcessor);
    }

}
