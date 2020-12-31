package com.sjq.live.endpoint.original;

import com.sjq.live.constant.LiveConfiguration;
import com.sjq.live.endpoint.AbstractTransformStreamEndpointHook;
import com.sjq.live.support.original.ServletInputStreamProcessor;
import com.sjq.live.support.original.ServletOutputStreamProcessor;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class OriginalTransformStreamEndpoint extends AbstractTransformStreamEndpointHook {

    /**
     * 原始流(供ffmpeg调用)
     * @param response
     * @throws Exception
     */
    @RequestMapping(LiveConfiguration.ORIGIN_STREAM_PATH)
    public void originStream(HttpServletResponse response, String publishId) throws Exception {
        response.getOutputStream().flush();
        originStreamReach(publishId, new ServletOutputStreamProcessor(response.getOutputStream()));
    }

    /**
     * 经ffmpeg转换后的流(供ffmpeg调用)
     * @param request
     * @throws Exception
     */
    @RequestMapping(LiveConfiguration.TRANSFORMED_STREAM_PATH)
    public void transformedStream(HttpServletRequest request, HttpServletResponse response, String publishId) throws Exception {
        transformedStreamReach(publishId, new ServletInputStreamProcessor(request.getInputStream()));
        response.setStatus(200);
    }
}
