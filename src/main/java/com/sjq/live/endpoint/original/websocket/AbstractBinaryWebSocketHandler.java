package com.sjq.live.endpoint.original.websocket;

import org.springframework.web.socket.handler.AbstractWebSocketHandler;

public abstract class AbstractBinaryWebSocketHandler extends AbstractWebSocketHandler {

    /**
     * websocket的enterPoint
     */
    private String[] paths;

    public AbstractBinaryWebSocketHandler(String...paths) {
        this.paths = paths;
    }

    public String[] getPaths() {
        return paths;
    }
}
