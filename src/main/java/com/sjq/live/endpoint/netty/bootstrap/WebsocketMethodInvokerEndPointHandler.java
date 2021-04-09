package com.sjq.live.endpoint.netty.bootstrap;

import com.sjq.live.model.LiveException;
import com.sjq.live.model.WebSocketAttribute;

public class WebsocketMethodInvokerEndPointHandler extends AbstractMethodInvokerHandler implements NettyWebsocketEndPointHandler {

    private Object instance;

    public WebsocketMethodInvokerEndPointHandler(Object instance) {
        this.instance = instance;
    }

    @Override
    public boolean beforeHandshake(WebSocketAttribute attribute) {
        return ((NettyWebsocketEndPointHandler) instance).beforeHandshake(attribute);
    }

    @Override
    public void handleMessage(byte[] data, boolean isLast) {
        EXECUTOR_SERVICE.execute(() -> {
            try {
                ((NettyWebsocketEndPointHandler) instance).handleMessage(data, isLast);
            } catch (Exception e) {
                throw new LiveException(e);
            }
        });
    }

    @Override
    public void handleMessage(String data, boolean isLast) {
        EXECUTOR_SERVICE.execute(() -> {
            try {
                ((NettyWebsocketEndPointHandler) instance).handleMessage(data, isLast);
            } catch (Exception e) {
                throw new LiveException(e);
            }
        });
    }

    @Override
    public void afterConnectionClosed(WebSocketAttribute attribute) {
        ((NettyWebsocketEndPointHandler) instance).afterConnectionClosed(attribute);
    }

}
