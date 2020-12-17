package com.sjq.live.endpoint.netty;

import com.sjq.live.endpoint.AbstractTransformStreamEndpointHook;
import com.sjq.live.endpoint.TransformStreamEndpointHook;
import com.sjq.live.service.TransformStream;
import com.sjq.live.support.InputStreamProcessor;
import com.sjq.live.support.OutputStreamProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;


@Component
@ConditionalOnProperty(value = "stream.transport", havingValue = "netty")
public class NettyTransformStreamEndpointHook extends AbstractTransformStreamEndpointHook {
}
