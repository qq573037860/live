package com.sjq.live.endpoint.original;

import com.sjq.live.endpoint.AbstractTransformStreamEndpointHook;
import com.sjq.live.support.original.ServletInputStreamProcessor;
import com.sjq.live.support.original.ServletOutputStreamProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@ConditionalOnProperty(value = "stream.transport", havingValue = "original")
public class OriginalTransformStreamEndpoint extends AbstractTransformStreamEndpointHook {

    /**
     * 原始流(供ffmpeg调用)
     * @param response
     * @throws Exception
     */
    @RequestMapping("/originStream")
    public void originStream(HttpServletResponse response, String publishId) throws Exception {
        originStreamReach(publishId, new ServletOutputStreamProcessor(response.getOutputStream()));
    }

    /**
     * 经ffmpeg转换后的流(供ffmpeg调用)
     * @param request
     * @throws Exception
     */
    @RequestMapping("/transformedStream")
    public void transformedStream(HttpServletRequest request, String publishId) throws Exception {
        while (request.getHeaderNames().hasMoreElements()) {
            System.out.print(request.getHeader(request.getHeaderNames().nextElement()) + " ");
        }
        System.out.println("-----------------------------------------------------------------");
        transformedStreamReach(publishId, new ServletInputStreamProcessor(request.getInputStream()));
    }

}
