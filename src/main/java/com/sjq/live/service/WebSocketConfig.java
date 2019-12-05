package com.sjq.live.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Created by shenjq on 2019/12/2
 */
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private WebSocketInterceptor interceptor;

    @Autowired
    private OriginStreamProcessor origin;
    @Autowired
    private LiveStreamProcessor live;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(origin, "ws/pushStream")
                .addHandler(live, "ws/receiveStream")
                .addInterceptors(interceptor)
                .setAllowedOrigins("*");
    }
}
