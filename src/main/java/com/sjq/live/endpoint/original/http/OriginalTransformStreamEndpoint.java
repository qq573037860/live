package com.sjq.live.endpoint.original.http;

import com.sjq.live.constant.LiveConfiguration;
import com.sjq.live.endpoint.original.OriginalEndPointSwitch;
import com.sjq.live.handler.TransformStreamHandler;
import com.sjq.live.support.original.ServletInputStreamProcessor;
import com.sjq.live.support.original.ServletOutputStreamProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@ConditionalOnBean(OriginalEndPointSwitch.class)
public class OriginalTransformStreamEndpoint{

    @Autowired
    private TransformStreamHandler transformStreamHandler;

    /**
     * 原始流(供ffmpeg调用)
     * @param response
     * @throws Exception
     */
    @RequestMapping(LiveConfiguration.ORIGIN_STREAM_PATH)
    public void originStream(HttpServletResponse response, String publishId) throws Exception {
        transformStreamHandler.processOriginalStream(publishId, new ServletOutputStreamProcessor(response.getOutputStream()));
    }

    /**
     * 经ffmpeg转换后的流(供ffmpeg调用)
     * @param request
     * @throws Exception
     */
    @RequestMapping(LiveConfiguration.TRANSFORMED_STREAM_PATH)
    public void transformedStream(HttpServletRequest request, HttpServletResponse response, String publishId) throws Exception {
        transformStreamHandler.processTransformedStream(publishId, new ServletInputStreamProcessor(request.getInputStream()));
    }
}
