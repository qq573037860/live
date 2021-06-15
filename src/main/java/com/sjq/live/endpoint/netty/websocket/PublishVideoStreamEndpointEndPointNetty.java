package com.sjq.live.endpoint.netty.websocket;

import com.sjq.live.constant.LiveConfiguration;
import com.sjq.live.endpoint.netty.NettyEndPointSwitch;
import com.sjq.live.endpoint.netty.bootstrap.NettyEndPoint;
import com.sjq.live.endpoint.netty.bootstrap.NettyWebsocketEndPointHandler;
import com.sjq.live.model.NettyWebsocketContext;
import com.sjq.live.model.RequestParam;
import com.sjq.live.service.VideoStreamHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.Objects;

@NettyEndPoint(path = LiveConfiguration.PUBLISH_PATH)
@Component
@ConditionalOnBean(NettyEndPointSwitch.class)
public class PublishVideoStreamEndpointEndPointNetty implements NettyWebsocketEndPointHandler {

    private static final Logger logger = LoggerFactory.getLogger(PublishVideoStreamEndpointEndPointNetty.class);

    @Autowired
    @Qualifier("defaultPublishVideoStreamHandler")
    private VideoStreamHandler videoStreamHandler;

    @Override
    public boolean beforeHandshake(final NettyWebsocketContext context) {
        final RequestParam requestParam = new RequestParam(context.getRequest().getAttribute());
        if (Objects.isNull(requestParam)
                || StringUtils.isEmpty(requestParam.getUserId())
                || StringUtils.isEmpty(requestParam.getPublishId())) {
            return false;
        }
        return true;
    }

    @Override
    public void afterConnectionEstablished(final NettyWebsocketContext context) {
        videoStreamHandler.afterConnectionEstablished(new RequestParam(context.getRequest().getAttribute()));
    }

    @Override
    public void handleMessage(final NettyWebsocketContext context, final byte[] data, final boolean isLast) {
        videoStreamHandler.handleBinaryMessage(new RequestParam(context.getRequest().getAttribute()), data);
    }

    @Override
    public void handleMessage(final NettyWebsocketContext context, final String data, final boolean isLast) {
    }

    @Override
    public void afterConnectionClosed(final NettyWebsocketContext context) {
        videoStreamHandler.afterConnectionClosed(new RequestParam(context.getRequest().getAttribute()));
    }
}
