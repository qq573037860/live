package com.sjq.live.endpoint.netty.bootstrap;

import com.sjq.live.model.WebSocketAttribute;

public interface NettyWebsocketEndPointHandler {

    boolean beforeHandshake(final WebSocketAttribute attribute);

    void handleMessage(final byte[] data,
                       final boolean isLast);

    void handleMessage(final String data,
                       final boolean isLast);

    void afterConnectionClosed(final WebSocketAttribute attribute);

}
