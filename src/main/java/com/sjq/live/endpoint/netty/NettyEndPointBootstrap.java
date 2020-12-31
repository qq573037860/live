package com.sjq.live.endpoint.netty;

import com.sjq.live.constant.LiveConfiguration;
import com.sjq.live.endpoint.netty.core.NettyHttpServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "stream.transport", havingValue = "netty")
public class NettyEndPointBootstrap {

    @Bean
    public NettyHttpServer initNettyHttpServer(@Autowired LiveConfiguration liveConfiguration) {
        return new NettyHttpServer(liveConfiguration);
    }
}
