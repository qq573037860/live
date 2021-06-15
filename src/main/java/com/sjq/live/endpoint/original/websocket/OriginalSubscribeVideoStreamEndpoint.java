package com.sjq.live.endpoint.original.websocket;

import com.sjq.live.constant.LiveConfiguration;
import com.sjq.live.endpoint.original.OriginalEndPointSwitch;
import com.sjq.live.service.VideoStreamHandler;
import com.sjq.live.support.AbstractStreamDistributeHandler;
import com.sjq.live.model.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

/**
 * Created by shenjq on 2019/12/2
 */

@Component
@ConditionalOnBean(OriginalEndPointSwitch.class)
public class OriginalSubscribeVideoStreamEndpoint extends AbstractBinaryWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(OriginalSubscribeVideoStreamEndpoint.class);

    @Autowired
    @Qualifier("defaultSubscribeVideoStreamHandler")
    private VideoStreamHandler videoStreamHandler;

    public OriginalSubscribeVideoStreamEndpoint() {
        super(LiveConfiguration.SUBSCRIBE_PATH);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        videoStreamHandler.afterConnectionEstablished(new RequestParam(session.getAttributes()), new StreamDistributeHandler(session));
        logger.info("client opened: " + session.toString());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        videoStreamHandler.afterConnectionEstablished(new RequestParam(session.getAttributes()));
        logger.info("client onclose：" + session.toString());
    }

    class StreamDistributeHandler extends AbstractStreamDistributeHandler {

        private final WebSocketSession session;

        StreamDistributeHandler(WebSocketSession session) {
            super();
            this.session = session;
        }

        @Override
        public void onData(final byte[] bytes) {
            if (!session.isOpen()) {
                logger.error("session:{}, 连接为关闭状态", session.toString());
                return;
            }
            try {
                session.sendMessage(new BinaryMessage(bytes, true));
            } catch (Exception e) {
                logger.error("session:{}，数据发送失败！", session.toString(), e);
                try {
                    session.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
