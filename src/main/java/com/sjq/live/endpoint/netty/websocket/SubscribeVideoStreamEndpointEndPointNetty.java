package com.sjq.live.endpoint.netty.websocket;

import com.sjq.live.constant.LiveConfiguration;
import com.sjq.live.endpoint.netty.NettyEndPointSwitch;
import com.sjq.live.endpoint.netty.bootstrap.NettyEndPoint;
import com.sjq.live.endpoint.netty.bootstrap.NettyWebsocketEndPointHandler;
import com.sjq.live.model.LiveException;
import com.sjq.live.model.NettyWebsocketContext;
import com.sjq.live.model.RequestParam;
import com.sjq.live.handler.VideoStreamHandler;
import com.sjq.live.support.AbstractStreamDistributeHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

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
        if (StringUtils.isEmpty(params.getUserId())
                || StringUtils.isEmpty(params.getSubscribeId())) {
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

        private int lengthCount = 0;

        StreamDistributeHandler(ChannelHandlerContext channelHandler) {
            super();
            this.channelHandler = channelHandler;
        }

        @Override
        public void onData(final byte[] bytes) {
            try {
                channelHandler.write(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(bytes)));
                /*if ((lengthCount += bytes.length) > 512) {
                    lengthCount = 0;*/
                    channelHandler.flush();
                //}
            } catch (Exception e) {
                logger.error("session:{}，数据发送失败！", channelHandler.toString(), e);
                channelHandler.close();
                throw new LiveException(e);
            }
        }
    }
}
