package com.sjq.live.endpoint.netty.websocket;

import com.sjq.live.constant.LiveConfiguration;
import com.sjq.live.endpoint.netty.NettyEndPointSwitch;
import com.sjq.live.endpoint.netty.bootstrap.NettyEndPoint;
import com.sjq.live.endpoint.netty.bootstrap.NettyWebsocketEndPointHandler;
import com.sjq.live.model.WebSocketAttribute;
import com.sjq.live.service.VideoStreamHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@NettyEndPoint(path = LiveConfiguration.PUBLISH_PATH)
@Component
@ConditionalOnBean(NettyEndPointSwitch.class)
public class NettyPublishVideoStreamEndpointEndPoint implements NettyWebsocketEndPointHandler {

    @Autowired
    @Qualifier("defaultPublishVideoStreamHandler")
    private VideoStreamHandler videoStreamHandler;

    @Override
    public boolean beforeHandshake(final WebSocketAttribute attribute) {
        return false;
    }

    @Override
    public void handleMessage(final byte[] data, final boolean isLast) {

    }

    @Override
    public void handleMessage(final String data, final boolean isLast) {

    }

    @Override
    public void afterConnectionClosed(final WebSocketAttribute attribute) {

    }
}
