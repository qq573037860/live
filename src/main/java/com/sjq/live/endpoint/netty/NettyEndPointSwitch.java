package com.sjq.live.endpoint.netty;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "stream.transport", havingValue = "netty")
public class NettyEndPointSwitch {
}
