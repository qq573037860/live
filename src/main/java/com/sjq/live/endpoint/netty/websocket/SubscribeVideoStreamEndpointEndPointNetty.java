package com.sjq.live.endpoint.netty.websocket;

import com.sjq.live.constant.LiveConfiguration;
import com.sjq.live.endpoint.netty.NettyEndPointSwitch;
import com.sjq.live.endpoint.netty.bootstrap.NettyEndPoint;
import com.sjq.live.endpoint.netty.bootstrap.NettyWebsocketEndPointHandler;
import com.sjq.live.model.NettyWebsocketContext;
import com.sjq.live.model.RequestParam;
import com.sjq.live.service.VideoStreamHandler;
import com.sjq.live.support.AbstractStreamDistributeHandler;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.Objects;

@NettyEndPoint(path = LiveConfiguration.SUBSCRIBE_PATH)
@Component
@ConditionalOnBean(NettyEndPointSwitch.class)
public class SubscribeVideoStreamEndpointEndPointNetty implements NettyWebsocketEndPointHandler {

    private static final Logger logger = LoggerFactory.getLogger(SubscribeVideoStreamEndpointEndPointNetty.class);

    @Autowired
    @Qualifier("defaultSubscribeVideoStreamHandler")
    private VideoStreamHandler videoStreamHandler;

    @Override
    public boolean beforeHandshake(final NettyWebsocketContext context) {
        final RequestParam params = new RequestParam(context.getRequest().getAttribute());
        if (Objects.isNull(params)
                || StringUtils.isEmpty(params.getUserId())
                && StringUtils.isEmpty(params.getSubscribeId())) {
            return false;
        }
        return true;
    }

    @Override
    public void afterConnectionEstablished(final NettyWebsocketContext context) {
        videoStreamHandler.afterConnectionEstablished(new RequestParam(context.getRequest().getAttribute()), new StreamDistributeHandler(context.getChannelHandler()));
        logger.info("client opened: " + context.toString());
    }

    @Override
    public void handleMessage(final NettyWebsocketContext context, byte[] data, boolean isLast) {
    }

    @Override
    public void handleMessage(final NettyWebsocketContext context, String data, boolean isLast) {
    }

    @Override
    public void afterConnectionClosed(final NettyWebsocketContext context) {
        videoStreamHandler.afterConnectionEstablished(new RequestParam(context.getRequest().getAttribute()));
        logger.info("client onclose：" + context.toString());
    }

    static class StreamDistributeHandler extends AbstractStreamDistributeHandler {

        private final ChannelHandlerContext channelHandler;

        StreamDistributeHandler(ChannelHandlerContext channelHandler) {
            super();
            this.channelHandler = channelHandler;
        }

        @Override
        public void onData(final byte[] bytes) {
            if (!channelHandler.channel().isOpen()) {
                logger.error("session:{}, 连接为关闭状态", channelHandler.toString());
                return;
            }
            try {
                channelHandler.writeAndFlush(bytes);
            } catch (Exception e) {
                logger.error("session:{}，数据发送失败！", channelHandler.toString(), e);
                channelHandler.close();
            }
        }
    }
}
