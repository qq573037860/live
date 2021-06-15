package com.sjq.live.endpoint.original.websocket.bootstrap;

import com.sjq.live.endpoint.original.OriginalEndPointSwitch;
import com.sjq.live.endpoint.original.websocket.AbstractBinaryWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import java.util.List;
import java.util.Optional;

/**
 * Created by shenjq on 2019/12/2
 */
@Configuration
@EnableWebSocket
@ConditionalOnBean(OriginalEndPointSwitch.class)
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private Optional<List<AbstractBinaryWebSocketHandler>> webSocketHandlers;
    @Autowired
    private List<WebSocketInterceptor> webSocketInterceptors;


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        webSocketHandlers.ifPresent(webSocketHandlers -> {
            webSocketHandlers.forEach(webSocketHandler -> {
                registry.addHandler(webSocketHandler, webSocketHandler.getPaths())
                        .addInterceptors(webSocketInterceptors.toArray(new WebSocketInterceptor[0]))
                        .setAllowedOrigins("*");
            });
        });
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(1024*1024);
        container.setMaxBinaryMessageBufferSize(1024*1024);
        return container;
    }
}