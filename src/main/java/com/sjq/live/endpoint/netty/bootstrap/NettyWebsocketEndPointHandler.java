package com.sjq.live.endpoint.netty.bootstrap;

import com.sjq.live.model.NettyWebsocketContext;

public interface NettyWebsocketEndPointHandler {

    boolean beforeHandshake(final NettyWebsocketContext context);

    void afterConnectionEstablished(final NettyWebsocketContext context);

    void handleMessage(final NettyWebsocketContext context,
                       final byte[] data,
                       final boolean isLast);

    void handleMessage(final NettyWebsocketContext context,
                       final String data,
                       final boolean isLast);

    void afterConnectionClosed(final NettyWebsocketContext context);

}
