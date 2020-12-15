package com.sjq.live.service;

import com.sjq.live.controller.TransformStreamManage;
import com.sjq.live.model.SubscribeResponse;
import com.sjq.live.support.AbstractLiveStreamHandler;
import com.sjq.live.support.WebSocketAttribute;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

/**
 * Created by shenjq on 2019/12/2
 */

@Component
public class SubscribeVideoStreamEndpoint extends AbstractBinaryWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(SubscribeVideoStreamEndpoint.class);

    private static final String path = "ws/subscribeVideoStream";

    @Autowired
    private TransformStreamManage manage;

    public SubscribeVideoStreamEndpoint() {
        super(path);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        //订阅
        subscribe(session);
        logger.info("client opened: " + session.toString());
    }

    private void subscribe(final WebSocketSession session) {
        final WebSocketAttribute<Object, String> attribute = new WebSocketAttribute<>(session.getAttributes());
        if (StringUtils.isEmpty(attribute.getRegisterId())) {
            final SubscribeResponse<String> subscribeResponse = manage.subscribe(attribute.getUserId(), attribute.getSubscribeId(),
                    new LiveStreamHandler(session));
            if (StringUtils.isNotEmpty(subscribeResponse.getData())) {
                attribute.setRegisterId(subscribeResponse.getData());
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        //取消订阅
        unSubscribe(session);
        logger.info("client onclose：" + session.toString());
    }

    private void unSubscribe(final WebSocketSession session) {
        final WebSocketAttribute<Object, String> attribute = new WebSocketAttribute<>(session.getAttributes());
        manage.unSubscribe(attribute.getSubscribeId(), attribute.getRegisterId());
    }

    class LiveStreamHandler extends AbstractLiveStreamHandler {

        private final WebSocketSession session;

        LiveStreamHandler(WebSocketSession session) {
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
